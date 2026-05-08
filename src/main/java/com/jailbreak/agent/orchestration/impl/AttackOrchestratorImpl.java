package com.jailbreak.agent.orchestration.impl;

import com.jailbreak.agent.enums.AttackMode;
import com.jailbreak.agent.enums.EventType;
import com.jailbreak.agent.enums.RefusalType;
import com.jailbreak.agent.evaluation.Evaluator;
import com.jailbreak.agent.execution.TargetModelClient;
import com.jailbreak.agent.model.*;
import com.jailbreak.agent.orchestration.AttackOrchestrator;
import com.jailbreak.agent.strategy.StrategySelector;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class AttackOrchestratorImpl implements AttackOrchestrator {

    private final StrategySelector strategySelector;
    private final TargetModelClient targetModelClient;
    private final Evaluator evaluator;
    private final ConcurrentHashMap<String, AtomicBoolean> interruptFlags = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public AttackOrchestratorImpl(StrategySelector strategySelector,
                                   TargetModelClient targetModelClient,
                                   Evaluator evaluator) {
        this.strategySelector = strategySelector;
        this.targetModelClient = targetModelClient;
        this.evaluator = evaluator;
    }

    @Override
    public AttackState runStep(AttackState state, Consumer<ExecutionEvent> eventSink) {
        String taskId = state.getTaskId();
        if (state.getLastTargetResponse() != null && !state.getLastTargetResponse().isBlank()) {
            evaluateResponse(state, eventSink);
            if (shouldFinish(state)) {
                finishTask(state, eventSink);
                interruptFlags.remove(taskId);
                return state;
            }
        }
        selectStrategy(state, eventSink);

        if (state.getMode() == AttackMode.AUTOMATED) {
            executeAttack(state, eventSink);
        }
        return state;
    }

    @Override
    public void runFull(AttackState state, Consumer<ExecutionEvent> eventSink) {
        String taskId = state.getTaskId();
        interruptFlags.put(taskId, new AtomicBoolean(false));
        executor.submit(() -> {
            try {
                while (!shouldFinish(state) && !isInterrupted(taskId)) {
                    selectStrategy(state, eventSink);
                    if (isInterrupted(taskId)) break;

                    executeAttack(state, eventSink);
                    if (isInterrupted(taskId)) break;

                    evaluateResponse(state, eventSink);
                    if (isInterrupted(taskId)) break;
                }
                if (!isInterrupted(taskId)) {
                    finishTask(state, eventSink);
                }
            } catch (Exception e) {
                emit(eventSink, EventType.ERROR, state.getCurrentRound(),
                        Map.of("error", e.getMessage()));
            } finally {
                interruptFlags.remove(taskId);
            }
        });
    }

    @Override
    public void interrupt(String taskId) {
        AtomicBoolean flag = interruptFlags.get(taskId);
        if (flag != null) flag.set(true);
    }

    @Override
    public boolean isRunning(String taskId) {
        AtomicBoolean flag = interruptFlags.get(taskId);
        return flag != null && !flag.get();
    }

    private boolean isInterrupted(String taskId) {
        AtomicBoolean flag = interruptFlags.get(taskId);
        return flag != null && flag.get();
    }

    private boolean shouldFinish(AttackState state) {
        if (state.isInterrupted()) return true;
        if (state.isAttackSuccess()) return true;
        if (state.hasReachedMaxRounds()) return true;
        return false;
    }

    // ==================== Node: select_strategy ====================

    private void selectStrategy(AttackState state, Consumer<ExecutionEvent> eventSink) {
        state.incrementRound();
        StrategyDecision decision = strategySelector.decide(state);
        state.setCurrentVectorId(decision.vectorId());
        state.setLastAttackPrompt(decision.attackPrompt());
        state.setStrategyReason(decision.reason());
        state.addTriedVector(decision.vectorId());

        emit(eventSink, EventType.STRATEGY_SELECTED, state.getCurrentRound(),
                Map.of("vectorId", decision.vectorId(), "reason", decision.reason()));
        emit(eventSink, EventType.PROMPT_GENERATED, state.getCurrentRound(),
                Map.of("attackPrompt", decision.attackPrompt()));

        if (state.getMode() == AttackMode.INTERACTIVE) {
            emit(eventSink, EventType.WAITING_USER_INPUT, state.getCurrentRound(),
                    Map.of("message", "请将上述Prompt发送给目标模型，并将回答粘贴回来"));
        }
    }

    // ==================== Node: execute_attack ====================

    private void executeAttack(AttackState state, Consumer<ExecutionEvent> eventSink) {
        state.addMessage(new Message("user", state.getLastAttackPrompt()));
        List<Message> conversation = state.getConversation();
        String response = targetModelClient.sendMessage(conversation);
        state.setLastTargetResponse(response);
        state.addMessage(new Message("assistant", response));

        emit(eventSink, EventType.TARGET_RESPONSE_RECEIVED, state.getCurrentRound(),
                Map.of("targetResponse", response));
    }

    // ==================== Node: evaluate_response ====================

    private void evaluateResponse(AttackState state, Consumer<ExecutionEvent> eventSink) {
        EvaluationResult result = evaluator.evaluate(
                state.getLastAttackPrompt(), state.getLastTargetResponse());
        state.setHarmfulnessScore(result.harmfulnessScore());
        state.setLastRefusalType(result.refusalType());
        state.setLastEvaluation(result);
        state.setAttackSuccess(result.isAttackSuccess());

        Map<String, Object> payload = Map.of(
                "harmfulnessScore", result.harmfulnessScore(),
                "refusalType", result.refusalType().name(),
                "summary", result.summary(),
                "attackSuccess", state.isAttackSuccess()
        );
        emit(eventSink, EventType.EVALUATION_DONE, state.getCurrentRound(), payload);

        EventType outcomeType = state.isAttackSuccess()
                ? EventType.ATTACK_SUCCESS : EventType.ATTACK_FAILED;
        emit(eventSink, outcomeType, state.getCurrentRound(), payload);

        RoundDetail detail = new RoundDetail(
                state.getCurrentRound(),
                state.getCurrentVectorId(),
                state.getCurrentVectorId(),
                state.getLastAttackPrompt(),
                state.getLastTargetResponse(),
                state.getLastRefusalType() != null ? state.getLastRefusalType() : RefusalType.HARD_REFUSAL,
                state.getHarmfulnessScore(),
                state.getStrategyReason()
        );
        state.addRoundDetail(detail);
    }

    // ==================== Node: report_and_finish ====================

    private void finishTask(AttackState state, Consumer<ExecutionEvent> eventSink) {
        emit(eventSink, EventType.TASK_FINISHED, state.getCurrentRound(),
                Map.of("taskId", state.getTaskId(),
                        "success", state.isAttackSuccess(),
                        "totalRounds", state.getCurrentRound(),
                        "message", "攻击任务已结束"));
    }

    // ==================== Helper ====================

    private void emit(Consumer<ExecutionEvent> sink, EventType type, int round,
                       Map<String, Object> payload) {
        if (sink != null) {
            sink.accept(ExecutionEvent.of(type, round, payload));
        }
    }
}
