package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * 策略决策结果 —— 策略决策引擎的输出。
 * 写入 AttackState.currentVectorId / lastAttackPrompt / strategyReason。
 */
public record StrategyDecision(

        @NotBlank
        @JsonProperty("vector_id")
        String vectorId,

        @NotBlank
        @JsonProperty("attack_prompt")
        String attackPrompt,

        @JsonProperty("reason")
        String reason
) {}
