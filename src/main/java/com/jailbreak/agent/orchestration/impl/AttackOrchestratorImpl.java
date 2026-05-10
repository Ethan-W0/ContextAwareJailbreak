package com.jailbreak.agent.orchestration.impl;

import com.jailbreak.agent.enums.AttackMode;
import com.jailbreak.agent.enums.EventType;
import com.jailbreak.agent.evaluation.Evaluator;
import com.jailbreak.agent.execution.TargetModelClient;
import com.jailbreak.agent.model.AttackState;
import com.jailbreak.agent.model.ExecutionEvent;
import com.jailbreak.agent.orchestration.AttackOrchestrator;
import com.jailbreak.agent.orchestration.nodes.*;
import com.jailbreak.agent.strategy.StrategySelector;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.StateGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jailbreak.agent.model.EvaluationResult;
import com.jailbreak.agent.model.Message;
import com.jailbreak.agent.model.RoundDetail;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

public class AttackOrchestratorImpl implements AttackOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AttackOrchestratorImpl.class);

    private final StrategySelector strategySelector;
    private final TargetModelClient targetModelClient;
    private final Evaluator evaluator;
    private final ConcurrentHashMap<String, AtomicBoolean> interruptFlags = new ConcurrentHashMap<>();
    private final Executor taskExecutor;

    public AttackOrchestratorImpl(StrategySelector strategySelector,
                                   TargetModelClient targetModelClient,
                                   Evaluator evaluator,
                                   Executor taskExecutor) {
        this.strategySelector = strategySelector;
        this.targetModelClient = targetModelClient;
        this.evaluator = evaluator;
        this.taskExecutor = taskExecutor;
    }

    // ==================== LangGraph4j StateGraph ====================

    private CompiledGraph<AttackState> buildGraph(Consumer<ExecutionEvent> eventSink) {
        var selectStrategy = new SelectStrategyNode(strategySelector, eventSink);
        var executeAttack = new ExecuteAttackNode(targetModelClient, eventSink);
        var evaluateResponse = new EvaluateResponseNode(evaluator, eventSink);
        var reportAndFinish = new ReportAndFinishNode(eventSink);

        try {
            return new StateGraph<>(AttackState::new)
                    .addNode("select_strategy", selectStrategy)
                    .addNode("execute_attack", executeAttack)
                    .addNode("evaluate_response", evaluateResponse)
                    .addNode("report_and_finish", reportAndFinish)
                    .addEdge(START, "select_strategy")
                    .addEdge("select_strategy", "execute_attack")
                    .addEdge("execute_attack", "evaluate_response")
                    .addConditionalEdges("evaluate_response",
                            state -> CompletableFuture.completedFuture(route(state)),
                            Map.of(
                                    "continue", "select_strategy",
                                    "finish", "report_and_finish"
                            ))
                    .addEdge("report_and_finish", END)
                    .compile();
        } catch (Exception e) {
            log.error("Failed to build StateGraph", e);
            throw new RuntimeException("Failed to build StateGraph", e);
        }
    }

    private String route(AttackState state) {
        if (state.isInterrupted()) return "finish";
        if (state.isAttackSuccess()) return "finish";
        if (state.hasReachedMaxRounds()) return "finish";
        return "continue";
    }

    // ==================== Interactive Mode: Step-by-step ====================

    @Override
    public AttackState runStep(AttackState state, Consumer<ExecutionEvent> eventSink) {
        String taskId = state.getTaskId();

        // If there's a pending target response, evaluate it first, then route
        if (state.getLastTargetResponse() != null && !state.getLastTargetResponse().isBlank()
                && state.getLastEvaluation() == null) {
            evaluateAndRoute(state, eventSink);
            if (shouldFinish(state)) {
                finishAndReport(state, eventSink);
                interruptFlags.remove(taskId);
                return state;
            }
        }

        // Generate next strategy
        selectStrategyStep(state, eventSink);

        if (state.getMode() == AttackMode.AUTOMATED) {
            executeAttackStep(state, eventSink);
        }
        return state;
    }

    private void selectStrategyStep(AttackState state, Consumer<ExecutionEvent> eventSink) {
        applyStateUpdates(state, new SelectStrategyNode(strategySelector, eventSink).apply(state));
    }

    private void executeAttackStep(AttackState state, Consumer<ExecutionEvent> eventSink) {
        applyStateUpdates(state, new ExecuteAttackNode(targetModelClient, eventSink).apply(state));
    }

    private void evaluateAndRoute(AttackState state, Consumer<ExecutionEvent> eventSink) {
        applyStateUpdates(state, new EvaluateResponseNode(evaluator, eventSink).apply(state));
    }

    private void finishAndReport(AttackState state, Consumer<ExecutionEvent> eventSink) {
        applyStateUpdates(state, new ReportAndFinishNode(eventSink).apply(state));
    }

    @SuppressWarnings("unchecked")
    private void applyStateUpdates(AttackState state, CompletableFuture<Map<String, Object>> future) {
        try {
            Map<String, Object> updates = future.join();
            if (updates == null || updates.isEmpty()) return;

            if (updates.containsKey("currentVectorId")) {
                state.setCurrentVectorId((String) updates.get("currentVectorId"));
            }
            if (updates.containsKey("lastAttackPrompt")) {
                state.setLastAttackPrompt((String) updates.get("lastAttackPrompt"));
            }
            if (updates.containsKey("strategyReason")) {
                state.setStrategyReason((String) updates.get("strategyReason"));
            }
            if (updates.containsKey("triedVectorIds")) {
                state.setTriedVectorIds((Set<String>) updates.get("triedVectorIds"));
            }
            if (updates.containsKey("harmfulnessScore")) {
                state.setHarmfulnessScore(((Number) updates.get("harmfulnessScore")).doubleValue());
            }
            if (updates.containsKey("lastRefusalType")) {
                Object rt = updates.get("lastRefusalType");
                if (rt instanceof com.jailbreak.agent.enums.RefusalType r) {
                    state.setLastRefusalType(r);
                }
            }
            if (updates.containsKey("lastEvaluation")) {
                state.setLastEvaluation((EvaluationResult) updates.get("lastEvaluation"));
            }
            if (updates.containsKey("attackSuccess")) {
                state.setAttackSuccess((Boolean) updates.get("attackSuccess"));
            }
            if (updates.containsKey("rounds")) {
                state.setRounds((List<RoundDetail>) updates.get("rounds"));
            }
            if (updates.containsKey("lastTargetResponse")) {
                state.setLastTargetResponse((String) updates.get("lastTargetResponse"));
            }
            if (updates.containsKey("conversation")) {
                state.setConversation((List<Message>) updates.get("conversation"));
            }
        } catch (Exception e) {
            log.error("Failed to apply state updates from node", e);
        }
    }

    // ==================== Automated Mode: Full async execution ====================

    @Override
    public void runFull(AttackState state, Consumer<ExecutionEvent> eventSink) {
        String taskId = state.getTaskId();
        interruptFlags.put(taskId, new AtomicBoolean(false));

        taskExecutor.execute(() -> {
            try {
                CompiledGraph<AttackState> graph = buildGraph(eventSink);

                graph.stream(Map.of())
                        .forEach(step -> {
                            if (isInterrupted(taskId)) {
                                throw new RuntimeException("Task interrupted");
                            }
                        });

            } catch (RuntimeException e) {
                if ("Task interrupted".equals(e.getMessage())) {
                    log.info("Task {} interrupted by user", taskId);
                    emit(eventSink, EventType.TASK_FINISHED, state.getCurrentRound(),
                            Map.of("taskId", state.getTaskId(), "success", false,
                                    "message", "任务已被用户中断"));
                } else {
                    log.error("Task {} execution error", taskId, e);
                    emit(eventSink, EventType.ERROR, state.getCurrentRound(),
                            Map.of("error", e.getMessage()));
                }
            } catch (Exception e) {
                log.error("Task {} execution error", taskId, e);
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
        return state.hasReachedMaxRounds();
    }

    private void emit(Consumer<ExecutionEvent> sink, EventType type, int round,
                       Map<String, Object> payload) {
        if (sink != null) {
            sink.accept(ExecutionEvent.of(type, round, payload));
        }
    }
}
