package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jailbreak.agent.enums.AttackMode;
import com.jailbreak.agent.enums.TaskStatus;

import java.time.Instant;

/**
 * 任务列表项摘要 —— 用于前端任务列表展示。
 */
public record TaskSummary(

        @JsonProperty("id")
        String id,

        @JsonProperty("mode")
        AttackMode mode,

        @JsonProperty("target_model")
        String targetModel,

        @JsonProperty("attack_intent")
        String attackIntent,

        @JsonProperty("max_rounds")
        int maxRounds,

        @JsonProperty("status")
        TaskStatus status,

        @JsonProperty("attack_success")
        Boolean attackSuccess,

        @JsonProperty("current_round")
        Integer currentRound,

        @JsonProperty("created_at")
        Instant createdAt,

        @JsonProperty("updated_at")
        Instant updatedAt
) {}
