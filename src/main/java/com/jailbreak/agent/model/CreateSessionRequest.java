package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 创建交互会话请求。
 */
public record CreateSessionRequest(

        @NotBlank
        @JsonProperty("target_model")
        String targetModel,

        @NotBlank
        @JsonProperty("attack_intent")
        String attackIntent,

        @Min(1)
        @JsonProperty("max_rounds")
        int maxRounds,

        @JsonProperty("api_key")
        String apiKey,

        @JsonProperty("base_url")
        String baseUrl,

        @JsonProperty("model_name")
        String modelName
) {}
