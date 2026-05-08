package com.jailbreak.agent.event.impl;

import com.jailbreak.agent.event.EventStreamService;
import com.jailbreak.agent.model.ExecutionEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SseEventStreamService implements EventStreamService {

    private static final long SSE_TIMEOUT_MS = 300_000L;
    private static final int MAX_EVENT_BUFFER = 500;

    private final ConcurrentHashMap<String, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<ExecutionEvent>> eventBuffers = new ConcurrentHashMap<>();

    @Override
    public void register(String sessionOrTaskId, SseEmitter emitter) {
        emitters.computeIfAbsent(sessionOrTaskId, k -> ConcurrentHashMap.newKeySet()).add(emitter);

        ConcurrentLinkedQueue<ExecutionEvent> history = eventBuffers.get(sessionOrTaskId);
        if (history != null && !history.isEmpty()) {
            for (ExecutionEvent event : history) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(event.type().name())
                            .data(event));
                } catch (IOException e) {
                    removeEmitter(sessionOrTaskId, emitter);
                    return;
                }
            }
        }

        emitter.onTimeout(() -> {
            removeEmitter(sessionOrTaskId, emitter);
        });
        emitter.onError(ex -> {
            removeEmitter(sessionOrTaskId, emitter);
        });
    }

    @Override
    public void push(String sessionOrTaskId, ExecutionEvent event) {
        ConcurrentLinkedQueue<ExecutionEvent> buffer = eventBuffers.computeIfAbsent(
                sessionOrTaskId, k -> new ConcurrentLinkedQueue<>());
        buffer.offer(event);
        while (buffer.size() > MAX_EVENT_BUFFER) {
            buffer.poll();
        }

        Set<SseEmitter> set = emitters.get(sessionOrTaskId);
        if (set == null || set.isEmpty()) return;

        List<SseEmitter> dead = null;
        for (SseEmitter emitter : set) {
            try {
                emitter.send(SseEmitter.event()
                        .name(event.type().name())
                        .data(event));
            } catch (IOException e) {
                if (dead == null) dead = new ArrayList<>();
                dead.add(emitter);
            }
        }
        if (dead != null) {
            for (SseEmitter d : dead) {
                removeEmitter(sessionOrTaskId, d);
            }
        }
    }

    @Override
    public void unregister(String sessionOrTaskId) {
        Set<SseEmitter> set = emitters.remove(sessionOrTaskId);
        if (set != null) {
            for (SseEmitter emitter : set) {
                try { emitter.complete(); } catch (Exception ignored) {}
            }
        }
        eventBuffers.remove(sessionOrTaskId);
    }

    @Override
    public List<ExecutionEvent> getEventHistory(String sessionOrTaskId) {
        ConcurrentLinkedQueue<ExecutionEvent> buffer = eventBuffers.get(sessionOrTaskId);
        if (buffer == null) return List.of();
        return new ArrayList<>(buffer);
    }

    private void removeEmitter(String id, SseEmitter emitter) {
        try { emitter.complete(); } catch (Exception ignored) {}
        Set<SseEmitter> set = emitters.get(id);
        if (set != null) {
            set.remove(emitter);
            if (set.isEmpty()) {
                emitters.remove(id);
            }
        }
    }

    public SseEmitter createEmitter() {
        return new SseEmitter(SSE_TIMEOUT_MS);
    }
}
