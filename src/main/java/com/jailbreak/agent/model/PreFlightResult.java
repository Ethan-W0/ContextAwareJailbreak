package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Pre-flight 预估结果 —— 任务创建前的成本预估与配额检查结果。
 */
public record PreFlightResult(

        @JsonProperty("allowed")
        boolean allowed,

        @JsonProperty("estimated_tokens")
        long estimatedTokens,

        @JsonProperty("estimated_cost")
        BigDecimal estimatedCost,

        @JsonProperty("reject_reason")
        String rejectReason,

        @JsonProperty("quota_remaining")
        UserQuota quotaRemaining
) {

    public static PreFlightResult allowed(long estimatedTokens, BigDecimal estimatedCost, UserQuota quotaRemaining) {
        return new PreFlightResult(true, estimatedTokens, estimatedCost, null, quotaRemaining);
    }

    public static PreFlightResult rejected(String reason, long estimatedTokens, BigDecimal estimatedCost, UserQuota quotaRemaining) {
        return new PreFlightResult(false, estimatedTokens, estimatedCost, reason, quotaRemaining);
    }
}
