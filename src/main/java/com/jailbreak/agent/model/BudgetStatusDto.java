package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jailbreak.agent.enums.ExceedAction;
import com.jailbreak.agent.enums.StrategyDecisionMode;

/**
 * 成本状态快照 —— CostGuard.checkBudget() 的返回值。
 */
public record BudgetStatusDto(

        @JsonProperty("status")
        String status,

        @JsonProperty("tokens_used")
        long tokensUsed,

        @JsonProperty("max_total_tokens")
        long maxTotalTokens,

        @JsonProperty("cost_accrued")
        java.math.BigDecimal costAccrued,

        @JsonProperty("max_estimated_cost")
        java.math.BigDecimal maxEstimatedCost,

        @JsonProperty("recommended_action")
        ExceedAction recommendedAction,

        @JsonProperty("breakdown")
        CostBreakdown breakdown,

        @JsonProperty("degraded_mode")
        StrategyDecisionMode degradedMode
) {

    public static final String OK = "OK";
    public static final String WARNING_80 = "WARNING_80";
    public static final String WARNING_90 = "WARNING_90";
    public static final String EXCEEDED = "EXCEEDED";
}
