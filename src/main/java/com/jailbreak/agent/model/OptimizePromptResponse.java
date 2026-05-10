package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OptimizePromptResponse(
        @JsonProperty("optimized_prompt") String optimizedPrompt,
        @JsonProperty("analysis") String analysis,
        @JsonProperty("weakness_found") String weaknessFound,
        @JsonProperty("improvement_strategy") String improvementStrategy
) {}
