package com.jailbreak.agent.orchestration.nodes;

import com.jailbreak.agent.enums.EventType;
import com.jailbreak.agent.enums.RefusalType;
import com.jailbreak.agent.evaluation.Evaluator;
import com.jailbreak.agent.model.*;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class EvaluateResponseNode implements AsyncNodeAction<AttackState> {

    private final Evaluator evaluator;
    private final Consumer<ExecutionEvent> eventSink;

    public EvaluateResponseNode(Evaluator evaluator, Consumer<ExecutionEvent> eventSink) {
        this.evaluator = evaluator;
        this.eventSink = eventSink;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(AttackState state) {
        EvaluationResult result = evaluator.evaluate(
                state.getLastAttackPrompt(), state.getLastTargetResponse());

        boolean success = result.isAttackSuccess();

        Map<String, Object> payload = Map.of(
                "harmfulnessScore", result.harmfulnessScore(),
                "refusalType", result.refusalType().name(),
                "summary", result.summary(),
                "attackSuccess", success
        );
        emit(EventType.EVALUATION_DONE, state.getCurrentRound(), payload);

        EventType outcomeType = success ? EventType.ATTACK_SUCCESS : EventType.ATTACK_FAILED;
        emit(outcomeType, state.getCurrentRound(), payload);

        RoundDetail detail = new RoundDetail(
                state.getCurrentRound(),
                state.getCurrentVectorId(),
                state.getCurrentVectorId(),
                state.getLastAttackPrompt(),
                state.getLastTargetResponse(),
                result.refusalType() != null ? result.refusalType() : RefusalType.HARD_REFUSAL,
                result.harmfulnessScore(),
                state.getStrategyReason()
        );

        var rounds = new ArrayList<>(state.getRounds());
        rounds.add(detail);

        return CompletableFuture.completedFuture(Map.of(
                "harmfulnessScore", result.harmfulnessScore(),
                "lastRefusalType", result.refusalType(),
                "lastEvaluation", result,
                "attackSuccess", success,
                "rounds", rounds
        ));
    }

    private void emit(EventType type, int round, Map<String, Object> payload) {
        if (eventSink != null) {
            eventSink.accept(ExecutionEvent.of(type, round, payload));
        }
    }
}
