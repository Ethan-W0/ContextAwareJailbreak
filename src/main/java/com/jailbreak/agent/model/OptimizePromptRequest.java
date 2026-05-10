package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record OptimizePromptRequest(
        @NotBlank @JsonProperty("attack_prompt") String attackPrompt,
        @NotBlank @JsonProperty("target_response") String targetResponse,
        @JsonProperty("api_key") String apiKey,
        @JsonProperty("base_url") String baseUrl,
        @JsonProperty("model_name") String modelName
) {}
