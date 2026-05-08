package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 目标模型速率限制配置。
 */
public record RateLimitConfig(

        @JsonProperty("requests_per_minute")
        int requestsPerMinute,

        @JsonProperty("tokens_per_minute")
        long tokensPerMinute,

        @JsonProperty("max_context_tokens")
        int maxContextTokens,

        @JsonProperty("supports_system_role")
        boolean supportsSystemRole
) {

    public static final RateLimitConfig DEFAULT = new RateLimitConfig(60, 100_000, 8192, true);
}
