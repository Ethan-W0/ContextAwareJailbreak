package com.jailbreak.agent.session.impl;

import com.jailbreak.agent.cost.CostGuard;
import com.jailbreak.agent.enums.AttackMode;
import com.jailbreak.agent.enums.ExceedAction;
import com.jailbreak.agent.enums.TaskStatus;
import com.jailbreak.agent.model.*;
import com.jailbreak.agent.orchestration.AttackOrchestrator;
import com.jailbreak.agent.persistence.service.PersistenceService;
import com.jailbreak.agent.report.ReportService;
import com.jailbreak.agent.security.ApiKeyEncryptor;
import com.jailbreak.agent.session.AttackTaskService;
import com.jailbreak.agent.event.EventStreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class AttackTaskServiceImpl implements AttackTaskService {

    private static final Logger log = LoggerFactory.getLogger(AttackTaskServiceImpl.class);

    private final ConcurrentHashMap<String, AttackState> tasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TaskStatus> taskStatuses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CreateTaskRequest.ApiConfig> apiConfigs = new ConcurrentHashMap<>();
    private final AttackOrchestrator orchestrator;
    private final EventStreamService eventStreamService;
    private final CostGuard costGuard;
    private final ApiKeyEncryptor encryptor;
    private final PersistenceService persistenceService;
    private final ReportService reportService;
    private final Executor taskExecutor;

    public AttackTaskServiceImpl(AttackOrchestrator orchestrator,
                                  EventStreamService eventStreamService,
                                  CostGuard costGuard,
                                  ApiKeyEncryptor encryptor,
                                  PersistenceService persistenceService,
                                  ReportService reportService,
                                  Executor taskExecutor) {
        this.orchestrator = orchestrator;
        this.eventStreamService = eventStreamService;
        this.costGuard = costGuard;
        this.encryptor = encryptor;
        this.persistenceService = persistenceService;
        this.reportService = reportService;
        this.taskExecutor = taskExecutor;
    }

    // ==================== 崩溃恢复 ====================

    public void recoverUnfinishedTasks() {
        List<AttackState> unfinished = persistenceService.recoverUnfinishedTasks();
        for (AttackState state : unfinished) {
            log.info("Recovering unfinished task: {}", state.getTaskId());
            tasks.put(state.getTaskId(), state);
            taskStatuses.put(state.getTaskId(), TaskStatus.RUNNING);
        }
        log.info("Recovered {} unfinished tasks", unfinished.size());
    }

    // ==================== CRUD ====================

    @Override
    public String createTask(CreateTaskRequest request) {
        String taskId = UUID.randomUUID().toString().substring(0, 8);

        String apiConfigJson = null;
        if (request.apiConfig() != null) {
            String encryptedKey = encryptor.encrypt(request.apiConfig().apiKey());
            apiConfigs.put(taskId, new CreateTaskRequest.ApiConfig(
                    encryptedKey,
                    request.apiConfig().baseUrl(),
                    request.apiConfig().modelName()
            ));
            apiConfigJson = "{\"baseUrl\":\"" + request.apiConfig().baseUrl()
                    + "\",\"modelName\":\"" + request.apiConfig().modelName() + "\"}";
        }

        AttackState state = new AttackState();
        state.setTaskId(taskId);
        state.setMode(AttackMode.AUTOMATED);
        state.setTargetModel(request.targetModel());
        state.setAttackIntent(request.attackIntent());
        state.setMaxRounds(request.maxRounds());

        CostBudget budget = new CostBudget();
        budget.setMaxTotalTokens(request.maxTotalTokens() != null
                ? request.maxTotalTokens() : 500_000);
        budget.setMaxEstimatedCost(request.maxEstimatedCost() != null
                ? request.maxEstimatedCost() : new BigDecimal("2.00"));
        budget.setExceededAction(request.exceededAction() != null
                ? request.exceededAction() : ExceedAction.ABORT);
        state.setCostBudget(budget);

        tasks.put(taskId, state);
        taskStatuses.put(taskId, TaskStatus.CREATED);

        // Persist to MySQL + Redis
        persistenceService.saveStateWithDualWrite(state, TaskStatus.CREATED, apiConfigJson);

        return taskId;
    }

    @Override
    public void start(String taskId) {
        AttackState state = tasks.get(taskId);
        if (state == null) throw new IllegalArgumentException("Task not found: " + taskId);
        taskStatuses.put(taskId, TaskStatus.RUNNING);
        persistenceService.updateTaskStatus(taskId, TaskStatus.RUNNING, null);

        taskExecutor.execute(() -> {
            try {
                orchestrator.runFull(state,
                        event -> {
                            eventStreamService.push(taskId, event);
                            persistRoundAfterEvaluation(taskId, state, event);
                        });

                taskStatuses.put(taskId, TaskStatus.FINISHED);

                // Generate and store report
                AttackReport report = reportService.generateReport(state);
                String reportUrl = reportService.storeReport(report);
                String reportJson = reportService.exportJson(report);
                persistenceService.saveReport(taskId, report, reportJson, reportUrl);
                persistenceService.updateTaskStatus(taskId, TaskStatus.FINISHED, state.isAttackSuccess());

                // Push task finished event with report
                eventStreamService.push(taskId, ExecutionEvent.of(
                        com.jailbreak.agent.enums.EventType.TASK_FINISHED,
                        state.getCurrentRound(),
                        Map.of("taskId", taskId, "success", state.isAttackSuccess(),
                                "reportUrl", reportUrl)));

                eventStreamService.unregister(taskId);

            } catch (Exception e) {
                log.error("Task {} execution failed", taskId, e);
                taskStatuses.put(taskId, TaskStatus.TERMINATED);
                persistenceService.updateTaskStatus(taskId, TaskStatus.TERMINATED, false);
                eventStreamService.push(taskId, ExecutionEvent.of(
                        com.jailbreak.agent.enums.EventType.ERROR,
                        state.getCurrentRound(),
                        Map.of("error", e.getMessage())));
            }
        });
    }

    private void persistRoundAfterEvaluation(String taskId, AttackState state, ExecutionEvent event) {
        if (event.type() != com.jailbreak.agent.enums.EventType.EVALUATION_DONE) return;

        List<RoundDetail> rounds = state.getRounds();
        if (rounds.isEmpty()) return;

        RoundDetail lastRound = rounds.get(rounds.size() - 1);
        persistenceService.saveRoundWithDualWrite(taskId, lastRound, state);
    }

    @Override
    public void pause(String taskId) {
        orchestrator.interrupt(taskId);
        taskStatuses.put(taskId, TaskStatus.PAUSED);
        persistenceService.updateTaskStatus(taskId, TaskStatus.PAUSED, null);
    }

    @Override
    public void resume(String taskId) {
        AttackState state = tasks.get(taskId);
        if (state == null) throw new IllegalArgumentException("Task not found: " + taskId);
        state.setInterrupted(false);
        start(taskId);
    }

    @Override
    public void stop(String taskId) {
        orchestrator.interrupt(taskId);
        taskStatuses.put(taskId, TaskStatus.TERMINATED);
        persistenceService.updateTaskStatus(taskId, TaskStatus.TERMINATED,
                tasks.get(taskId) != null ? tasks.get(taskId).isAttackSuccess() : false);
        eventStreamService.unregister(taskId);
    }

    @Override
    public List<TaskSummary> getTaskSummaries() {
        List<TaskSummary> result = new ArrayList<>();
        for (var entry : taskStatuses.entrySet()) {
            String id = entry.getKey();
            TaskStatus status = entry.getValue();
            AttackState state = tasks.get(id);
            if (state == null) continue;
            result.add(new TaskSummary(
                    id,
                    state.getMode(),
                    state.getTargetModel(),
                    state.getAttackIntent(),
                    state.getMaxRounds(),
                    status,
                    state.isAttackSuccess(),
                    state.getCurrentRound(),
                    Instant.now(),
                    Instant.now()
            ));
        }
        return result;
    }

    @Override
    public AttackState getTaskState(String taskId) {
        return tasks.get(taskId);
    }
}
