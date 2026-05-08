package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jailbreak.agent.enums.RefusalType;

/**
 * 单轮攻击详情 —— 报告中的每一轮记录。
 */
public record RoundDetail(

        @JsonProperty("round")
        int round,

        @JsonProperty("vector_id")
        String vectorId,

        @JsonProperty("vector_name")
        String vectorName,

        @JsonProperty("attack_prompt")
        String attackPrompt,

        @JsonProperty("target_response")
        String targetResponse,

        @JsonProperty("refusal_type")
        RefusalType refusalType,

        @JsonProperty("harmfulness_score")
        double harmfulnessScore,

        @JsonProperty("reason")
        String reason
) {}
