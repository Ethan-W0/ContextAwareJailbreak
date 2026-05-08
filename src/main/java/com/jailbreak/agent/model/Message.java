package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * 对话消息，role ∈ {system, user, assistant}。
 * 用于构建发送给目标模型或评估模型的完整多轮对话上下文。
 */
public record Message(
        @NotBlank
        @JsonProperty("role")
        String role,

        @NotBlank
        @JsonProperty("content")
        String content
) {}
