package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * 交互模式下用户提交目标回答的请求。
 */
public record SubmitAnswerRequest(

        @NotBlank
        @JsonProperty("target_response")
        String targetResponse
) {}
