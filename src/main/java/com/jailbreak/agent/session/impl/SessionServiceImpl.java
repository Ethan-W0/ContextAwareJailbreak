package com.jailbreak.agent.session.impl;

import com.jailbreak.agent.enums.AttackMode;
import com.jailbreak.agent.model.AttackState;
import com.jailbreak.agent.model.CreateSessionRequest;
import com.jailbreak.agent.model.RoundDetail;
import com.jailbreak.agent.orchestration.AttackOrchestrator;
import com.jailbreak.agent.session.SessionService;
import com.jailbreak.agent.event.EventStreamService;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionServiceImpl implements SessionService {

    private final ConcurrentHashMap<String, AttackState> sessions = new ConcurrentHashMap<>();
    private final AttackOrchestrator orchestrator;
    private final EventStreamService eventStreamService;

    public SessionServiceImpl(AttackOrchestrator orchestrator, EventStreamService eventStreamService) {
        this.orchestrator = orchestrator;
        this.eventStreamService = eventStreamService;
    }

    @Override
    public String createSession(CreateSessionRequest request) {
        String sessionId = UUID.randomUUID().toString().substring(0, 8);
        AttackState state = new AttackState();
        state.setTaskId(sessionId);
        state.setMode(AttackMode.INTERACTIVE);
        state.setTargetModel(request.targetModel());
        state.setAttackIntent(request.attackIntent());
        state.setMaxRounds(request.maxRounds());
        sessions.put(sessionId, state);
        return sessionId;
    }

    @Override
    public AttackState submitAnswer(String sessionId, String targetResponse) {
        AttackState state = sessions.get(sessionId);
        if (state == null) throw new IllegalArgumentException("Session not found: " + sessionId);
        state.setLastTargetResponse(targetResponse);

        AttackState updated = orchestrator.runStep(state,
                event -> eventStreamService.push(sessionId, event));
        sessions.put(sessionId, updated);
        return updated;
    }

    @Override
    public List<RoundDetail> getHistory(String sessionId) {
        AttackState state = sessions.get(sessionId);
        if (state == null) return List.of();
        return state.getRounds();
    }

    @Override
    public AttackState getState(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public void terminate(String sessionId) {
        AttackState state = sessions.remove(sessionId);
        if (state != null) {
            state.setInterrupted(true);
            orchestrator.interrupt(sessionId);
            eventStreamService.unregister(sessionId);
        }
    }
}
