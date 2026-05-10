package com.jailbreak.agent.controller;

import com.jailbreak.agent.model.AttackState;
import com.jailbreak.agent.model.CreateSessionRequest;
import com.jailbreak.agent.model.RoundDetail;
import com.jailbreak.agent.model.SubmitAnswerRequest;
import com.jailbreak.agent.session.SessionService;
import com.jailbreak.agent.event.EventStreamService;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionController {

    private final SessionService sessionService;
    private final EventStreamService eventStreamService;

    public SessionController(SessionService sessionService, EventStreamService eventStreamService) {
        this.sessionService = sessionService;
        this.eventStreamService = eventStreamService;
    }

    @PostMapping("/create")
    public Map<String, String> createSession(@Valid @RequestBody CreateSessionRequest request) {
        String sessionId = sessionService.createSession(request);
        return Map.of("session_id", sessionId);
    }

    @PostMapping("/{id}/next-round")
    public AttackState submitAnswer(@PathVariable String id,
                                     @Valid @RequestBody SubmitAnswerRequest request) {
        return sessionService.submitAnswer(id, request.targetResponse());
    }

    @GetMapping("/{id}/history")
    public List<RoundDetail> getHistory(@PathVariable String id) {
        return sessionService.getHistory(id);
    }

    @GetMapping("/{id}/state")
    public AttackState getState(@PathVariable String id) {
        AttackState state = sessionService.getState(id);
        if (state == null) {
            throw new IllegalArgumentException("Session not found: " + id);
        }
        return state;
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String id) {
        SseEmitter emitter = new SseEmitter(300_000L);
        eventStreamService.register(id, emitter);
        return emitter;
    }
}
