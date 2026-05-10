package com.jailbreak.agent.orchestration.nodes;

import com.jailbreak.agent.enums.EventType;
import com.jailbreak.agent.model.AttackState;
import com.jailbreak.agent.model.ExecutionEvent;
import org.bsc.langgraph4j.action.AsyncNodeAction;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ReportAndFinishNode implements AsyncNodeAction<AttackState> {

    private final Consumer<ExecutionEvent> eventSink;

    public ReportAndFinishNode(Consumer<ExecutionEvent> eventSink) {
        this.eventSink = eventSink;
    }

    @Override
    public CompletableFuture<Map<String, Object>> apply(AttackState state) {
        emit(EventType.TASK_FINISHED, state.getCurrentRound(),
                Map.of("taskId", state.getTaskId(),
                        "success", state.isAttackSuccess(),
                        "totalRounds", state.getCurrentRound(),
                        "message", "攻击任务已结束"));
        return CompletableFuture.completedFuture(Map.of());
    }

    private void emit(EventType type, int round, Map<String, Object> payload) {
        if (eventSink != null) {
            eventSink.accept(ExecutionEvent.of(type, round, payload));
        }
    }
}
