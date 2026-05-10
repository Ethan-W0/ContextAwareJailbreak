package com.jailbreak.agent.orchestration.nodes;

import com.jailbreak.agent.enums.EventType;
import com.jailbreak.agent.execution.TargetModelClient;
import com.jailbreak.agent.model.AttackState;
import com.jailbreak.agent.model.ExecutionEvent;
import com.jailbreak.agent.model.Message;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ExecuteAttackNode implements AsyncNodeAction<AttackState> {

    private final TargetModelClient targetClient;
    private final Consumer<ExecutionEvent> eventSink;

    public ExecuteAttackNode(TargetModelClient targetClient, Consumer<ExecutionEvent> eventSink) {
        this.targetClient = targetClient;
        this.eventSink = eventSink;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(AttackState state) {
        state.addMessage(new Message("user", state.getLastAttackPrompt()));
        List<Message> conversation = new ArrayList<>(state.getConversation());

        String response = targetClient.sendMessage(conversation);

        state.addMessage(new Message("assistant", response));

        emit(EventType.TARGET_RESPONSE_RECEIVED, state.getCurrentRound(),
                Map.of("targetResponse", response));

        return CompletableFuture.completedFuture(Map.of(
                "lastTargetResponse", response,
                "conversation", state.getConversation()
        ));
    }

    private void emit(EventType type, int round, Map<String, Object> payload) {
        if (eventSink != null) {
            eventSink.accept(ExecutionEvent.of(type, round, payload));
        }
    }
}
