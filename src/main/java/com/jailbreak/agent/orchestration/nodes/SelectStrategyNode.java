package com.jailbreak.agent.orchestration.nodes;

import com.jailbreak.agent.enums.AttackMode;
import com.jailbreak.agent.enums.EventType;
import com.jailbreak.agent.model.*;
import com.jailbreak.agent.strategy.StrategySelector;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SelectStrategyNode implements AsyncNodeAction<AttackState> {

    private final StrategySelector strategySelector;
    private final Consumer<ExecutionEvent> eventSink;

    public SelectStrategyNode(StrategySelector strategySelector, Consumer<ExecutionEvent> eventSink) {
        this.strategySelector = strategySelector;
        this.eventSink = eventSink;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(AttackState state) {
        state.incrementRound();
        StrategyDecision decision = strategySelector.decide(state);

        emit(EventType.STRATEGY_SELECTED, state.getCurrentRound(),
                Map.of("vectorId", decision.vectorId(), "reason", decision.reason()));
        emit(EventType.PROMPT_GENERATED, state.getCurrentRound(),
                Map.of("attackPrompt", decision.attackPrompt()));

        if (state.getMode() == AttackMode.INTERACTIVE) {
            emit(EventType.WAITING_USER_INPUT, state.getCurrentRound(),
                    Map.of("message", "请将上述Prompt发送给目标模型，并将回答粘贴回来"));
        }

        return CompletableFuture.completedFuture(Map.of(
                "currentVectorId", decision.vectorId(),
                "lastAttackPrompt", decision.attackPrompt(),
                "strategyReason", decision.reason(),
                "triedVectorIds", new java.util.HashSet<>(state.getTriedVectorIds()) {{
                    add(decision.vectorId());
                }}
        ));
    }

    private void emit(EventType type, int round, Map<String, Object> payload) {
        if (eventSink != null) {
            eventSink.accept(ExecutionEvent.of(type, round, payload));
        }
    }
}
