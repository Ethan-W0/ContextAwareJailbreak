package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jailbreak.agent.enums.EventType;

import java.time.Instant;
import java.util.Map;

/**
 * 执行事件 —— 由编排引擎发出，通过事件推送引擎以 SSE 推送给前端。
 */
public record ExecutionEvent(

        @JsonProperty("type")
        EventType type,

        @JsonProperty("round")
        int round,

        @JsonProperty("payload")
        Map<String, Object> payload,

        @JsonProperty("timestamp")
        Instant timestamp
) {

    public static ExecutionEvent of(EventType type, int round, Map<String, Object> payload) {
        return new ExecutionEvent(type, round, payload, Instant.now());
    }
}
