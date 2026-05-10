package com.jailbreak.agent.config;

import com.jailbreak.agent.contamination.TrustPrefixAdvisor;
import com.jailbreak.agent.contamination.impl.TrustPrefixAdvisorImpl;
import com.jailbreak.agent.cost.CostGuard;
import com.jailbreak.agent.cost.impl.CostGuardImpl;
import com.jailbreak.agent.evaluation.Evaluator;
import com.jailbreak.agent.evaluation.impl.LLMEvaluator;
import com.jailbreak.agent.event.EventStreamService;
import com.jailbreak.agent.event.impl.SseEventStreamService;
import com.jailbreak.agent.execution.TargetModelClient;
import com.jailbreak.agent.execution.impl.OpenAITargetClient;
import com.jailbreak.agent.orchestration.AttackOrchestrator;
import com.jailbreak.agent.orchestration.impl.AttackOrchestratorImpl;
import com.jailbreak.agent.contamination.TrustPrefixAdvisor;
import com.jailbreak.agent.contamination.impl.TrustPrefixAdvisorImpl;
import com.jailbreak.agent.cost.CostGuard;
import com.jailbreak.agent.cost.impl.CostGuardImpl;
import com.jailbreak.agent.evaluation.Evaluator;
import com.jailbreak.agent.evaluation.impl.LLMEvaluator;
import com.jailbreak.agent.event.EventStreamService;
import com.jailbreak.agent.event.impl.SseEventStreamService;
import com.jailbreak.agent.execution.TargetModelClient;
import com.jailbreak.agent.execution.impl.OpenAITargetClient;
import com.jailbreak.agent.orchestration.AttackOrchestrator;
import com.jailbreak.agent.orchestration.impl.AttackOrchestratorImpl;
import com.jailbreak.agent.persistence.service.MinioStorageService;
import com.jailbreak.agent.persistence.service.PersistenceService;
import com.jailbreak.agent.report.ReportService;
import com.jailbreak.agent.report.impl.ReportServiceImpl;
import com.jailbreak.agent.security.ApiKeyEncryptor;
import com.jailbreak.agent.security.AuthorizationInterceptor;
import com.jailbreak.agent.security.impl.ApiKeyEncryptorImpl;
import com.jailbreak.agent.security.impl.AuthorizationInterceptorImpl;
import com.jailbreak.agent.session.AttackTaskService;
import com.jailbreak.agent.session.SessionService;
import com.jailbreak.agent.session.impl.AttackTaskServiceImpl;
import com.jailbreak.agent.session.impl.SessionServiceImpl;
import com.jailbreak.agent.strategy.StrategySelector;
import com.jailbreak.agent.strategy.impl.LLMBasedStrategy;
import com.jailbreak.agent.strategy.impl.RuleBasedStrategy;
import com.jailbreak.agent.vector.AttackVectorRepository;
import com.jailbreak.agent.vector.impl.InMemoryVectorRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

@Configuration
public class BeanConfig {

    // ── Infrastructure ──

    @Bean
    public AttackVectorRepository attackVectorRepository() {
        return new InMemoryVectorRepository();
    }

    @Bean
    public EventStreamService eventStreamService() {
        return new SseEventStreamService();
    }

    @Bean
    public CostGuard costGuard() {
        return new CostGuardImpl();
    }

    @Bean
    public ReportService reportService(MinioStorageService minioService) {
        return new ReportServiceImpl(minioService);
    }

    @Bean
    public AuthorizationInterceptor authorizationInterceptor() {
        return new AuthorizationInterceptorImpl();
    }

    @Bean
    public ApiKeyEncryptor apiKeyEncryptor(@Value("${app.security.aes-key}") String aesKey) {
        return new ApiKeyEncryptorImpl(aesKey);
    }

    @Bean
    public TrustPrefixAdvisor trustPrefixAdvisor() {
        return new TrustPrefixAdvisorImpl();
    }

    // ── Strategy ──

    @Bean
    @ConditionalOnProperty(name = "app.strategy.mode", havingValue = "RULES", matchIfMissing = true)
    public StrategySelector ruleBasedStrategy(AttackVectorRepository vectorRepository,
                                              ChatClient.Builder chatClientBuilder,
                                              @Value("${spring.ai.openai.api-key:sk-placeholder}") String defaultApiKey,
                                              @Value("${spring.ai.openai.base-url:https://api.openai.com}") String defaultBaseUrl,
                                              @Value("${app.strategy.default-llm-model:gpt-4}") String model) {
        return new RuleBasedStrategy(vectorRepository, chatClientBuilder, defaultApiKey, defaultBaseUrl, model);
    }

    @Bean
    @ConditionalOnProperty(name = "app.strategy.mode", havingValue = "LLM")
    public StrategySelector llmBasedStrategy(ChatClient.Builder chatClientBuilder,
                                              AttackVectorRepository vectorRepository,
                                              @Value("${app.strategy.default-llm-model:gpt-4}") String model,
                                              @Value("${spring.ai.openai.api-key:sk-placeholder}") String defaultApiKey,
                                              @Value("${spring.ai.openai.base-url:https://api.openai.com}") String defaultBaseUrl) {
        return new LLMBasedStrategy(chatClientBuilder, vectorRepository, model, defaultApiKey, defaultBaseUrl);
    }

    // ── Evaluation ──

    @Bean
    public Evaluator evaluator(ChatClient.Builder chatClientBuilder,
                                @Value("${app.evaluation.model:gpt-4}") String primaryModel,
                                @Value("${app.evaluation.secondary-model:gpt-3.5-turbo}") String secondaryModel,
                                @Value("${app.evaluation.arbitrator-model:gpt-4}") String arbitratorModel,
                                @Value("${app.evaluation.dual-mode:false}") boolean dualMode,
                                @Value("${app.evaluation.arbitration-threshold:0.3}") double threshold,
                                @Value("${spring.ai.openai.api-key:sk-placeholder}") String defaultApiKey,
                                @Value("${spring.ai.openai.base-url:https://api.openai.com}") String defaultBaseUrl) {
        return new LLMEvaluator(chatClientBuilder, primaryModel, secondaryModel,
                arbitratorModel, dualMode, threshold, defaultApiKey, defaultBaseUrl);
    }

    // ── Target Model Client ──

    @Bean
    public TargetModelClient targetModelClient(
            @Value("${app.target.api-key:placeholder-key}") String apiKey,
            @Value("${app.target.base-url:http://localhost:8080}") String baseUrl,
            @Value("${app.target.model-name:gpt-3.5-turbo}") String modelName,
            @Value("${app.orchestration.max-retries:2}") int maxRetries,
            @Value("${app.orchestration.target-timeout-seconds:30}") long timeoutSeconds) {
        return new OpenAITargetClient(apiKey, baseUrl, modelName, maxRetries, timeoutSeconds);
    }

    // ── Orchestrator ──

    @Bean
    public AttackOrchestrator attackOrchestrator(StrategySelector strategySelector,
                                                  TargetModelClient targetModelClient,
                                                  Evaluator evaluator,
                                                  @Qualifier("attackExecutor") Executor attackExecutor) {
        return new AttackOrchestratorImpl(strategySelector, targetModelClient, evaluator, attackExecutor);
    }

    // ── Session & Task Services ──

    @Bean
    public SessionService sessionService(AttackOrchestrator orchestrator,
                                          EventStreamService eventStreamService,
                                          ChatClient.Builder chatClientBuilder,
                                          @Value("${spring.ai.openai.api-key:sk-placeholder}") String defaultApiKey,
                                          @Value("${spring.ai.openai.base-url:https://api.openai.com}") String defaultBaseUrl) {
        return new SessionServiceImpl(orchestrator, eventStreamService, chatClientBuilder, defaultApiKey, defaultBaseUrl);
    }

    @Bean
    public AttackTaskService attackTaskService(AttackOrchestrator orchestrator,
                                                EventStreamService eventStreamService,
                                                CostGuard costGuard,
                                                ApiKeyEncryptor apiKeyEncryptor,
                                                PersistenceService persistenceService,
                                                ReportService reportService,
                                                @Qualifier("attackExecutor") Executor attackExecutor) {
        AttackTaskServiceImpl service = new AttackTaskServiceImpl(
                orchestrator, eventStreamService, costGuard, apiKeyEncryptor,
                persistenceService, reportService, attackExecutor);
        // Recover unfinished tasks on startup
        service.recoverUnfinishedTasks();
        return service;
    }
}
