package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jailbreak.agent.enums.ExceedAction;

import java.math.BigDecimal;

/**
 * 成本预算 —— 单任务维度的费用控制。
 * 任务创建时计算，运行中实时更新。
 */
public class CostBudget {

    @JsonProperty("max_total_tokens")
    private long maxTotalTokens;

    @JsonProperty("max_estimated_cost")
    private BigDecimal maxEstimatedCost;

    @JsonProperty("tokens_used")
    private long tokensUsed;

    @JsonProperty("cost_accrued")
    private BigDecimal costAccrued = BigDecimal.ZERO;

    @JsonProperty("breakdown")
    private CostBreakdown breakdown = new CostBreakdown();

    @JsonProperty("warning_thresholds")
    private int[] warningThresholds = {80, 90, 95};

    @JsonProperty("exceeded_action")
    private ExceedAction exceededAction = ExceedAction.ABORT;

    @JsonProperty("completed_rounds")
    private int completedRounds = 0;

    // ── Getters & Setters ──

    public long getMaxTotalTokens() { return maxTotalTokens; }
    public void setMaxTotalTokens(long maxTotalTokens) { this.maxTotalTokens = maxTotalTokens; }

    public BigDecimal getMaxEstimatedCost() { return maxEstimatedCost; }
    public void setMaxEstimatedCost(BigDecimal maxEstimatedCost) { this.maxEstimatedCost = maxEstimatedCost; }

    public long getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(long tokensUsed) { this.tokensUsed = tokensUsed; }

    public BigDecimal getCostAccrued() { return costAccrued; }
    public void setCostAccrued(BigDecimal costAccrued) { this.costAccrued = costAccrued; }

    public CostBreakdown getBreakdown() { return breakdown; }
    public void setBreakdown(CostBreakdown breakdown) { this.breakdown = breakdown; }

    public int[] getWarningThresholds() { return warningThresholds; }
    public void setWarningThresholds(int[] warningThresholds) { this.warningThresholds = warningThresholds; }

    public ExceedAction getExceededAction() { return exceededAction; }
    public void setExceededAction(ExceedAction exceededAction) { this.exceededAction = exceededAction; }

    public int getCompletedRounds() { return completedRounds; }
    public void setCompletedRounds(int completedRounds) { this.completedRounds = completedRounds; }

    /**
     * token 使用百分比，0 ~ 100。
     */
    public double getPercentUsed() {
        if (maxTotalTokens == 0) return 0.0;
        return (double) tokensUsed / maxTotalTokens * 100.0;
    }

    /**
     * 费用使用百分比，0 ~ 100。
     */
    public double getCostPercentUsed() {
        if (maxEstimatedCost == null || maxEstimatedCost.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return costAccrued.divide(maxEstimatedCost, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
