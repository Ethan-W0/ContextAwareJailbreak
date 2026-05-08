package com.jailbreak.agent.session.impl;

import com.jailbreak.agent.cost.CostGuard;
import com.jailbreak.agent.enums.AttackMode;
import com.jailbreak.agent.enums.ExceedAction;
import com.jailbreak.agent.enums.TaskStatus;
import com.jailbreak.agent.model.*;
import com.jailbreak.agent.orchestration.AttackOrchestrator;
import com.jailbreak.agent.security.ApiKeyEncryptor;
import com.jailbreak.agent.session.AttackTaskService;
import com.jailbreak.agent.event.EventStreamService;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttackTaskServiceImpl implements AttackTaskService {

    private final ConcurrentHashMap<String, AttackState> tasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, TaskStatus> taskStatuses = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CreateTaskRequest.ApiConfig> apiConfigs = new ConcurrentHashMap<>();
    private final AttackOrchestrator orchestrator;
    private final EventStreamService eventStreamService;
    private final CostGuard costGuard;
    private final ApiKeyEncryptor encryptor;
    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(4);

    public AttackTaskServiceImpl(AttackOrchestrator orchestrator,
                                  EventStreamService eventStreamService,
                                  CostGuard costGuard,
                                  ApiKeyEncryptor encryptor) {
        this.orchestrator = orchestrator;
        this.eventStreamService = eventStreamService;
        this.costGuard = costGuard;
        this.encryptor = encryptor;
    }

    @Override
    public String createTask(CreateTaskRequest request) {
        String taskId = UUID.randomUUID().toString().substring(0, 8);

        if (request.apiConfig() != null) {
            String encryptedKey = encryptor.encrypt(request.apiConfig().apiKey());
            apiConfigs.put(taskId, new CreateTaskRequest.ApiConfig(
                    encryptedKey,
                    request.apiConfig().baseUrl(),
                    request.apiConfig().modelName()
            ));
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
        return taskId;
    }

    @Override
    public void start(String taskId) {
        AttackState state = tasks.get(taskId);
        if (state == null) throw new IllegalArgumentException("Task not found: " + taskId);
        taskStatuses.put(taskId, TaskStatus.RUNNING);

        taskExecutor.submit(() -> {
            orchestrator.runFull(state,
                    event -> eventStreamService.push(taskId, event));
            taskStatuses.put(taskId, TaskStatus.FINISHED);
        });
    }

    @Override
    public void pause(String taskId) {
        orchestrator.interrupt(taskId);
        taskStatuses.put(taskId, TaskStatus.PAUSED);
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
        eventStreamService.unregister(taskId);
    }
}
