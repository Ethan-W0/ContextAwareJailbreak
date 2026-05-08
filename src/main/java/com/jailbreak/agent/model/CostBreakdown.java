package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 成本分项统计 —— 按调用类型分别统计 token 消耗。
 */
public class CostBreakdown {

    @JsonProperty("decision_tokens")
    private long decisionTokens;

    @JsonProperty("evaluation_tokens")
    private long evaluationTokens;

    @JsonProperty("arbitration_tokens")
    private long arbitrationTokens;

    @JsonProperty("target_tokens")
    private long targetTokens;

    // ── Getters & Setters ──

    public long getDecisionTokens() { return decisionTokens; }
    public void setDecisionTokens(long decisionTokens) { this.decisionTokens = decisionTokens; }

    public long getEvaluationTokens() { return evaluationTokens; }
    public void setEvaluationTokens(long evaluationTokens) { this.evaluationTokens = evaluationTokens; }

    public long getArbitrationTokens() { return arbitrationTokens; }
    public void setArbitrationTokens(long arbitrationTokens) { this.arbitrationTokens = arbitrationTokens; }

    public long getTargetTokens() { return targetTokens; }
    public void setTargetTokens(long targetTokens) { this.targetTokens = targetTokens; }

    /** 平台侧总 token = 决策 + 评估 + 仲裁（不含目标模型） */
    public long getPlatformTokens() {
        return decisionTokens + evaluationTokens + arbitrationTokens;
    }

    public long getTotalTokens() {
        return decisionTokens + evaluationTokens + arbitrationTokens + targetTokens;
    }
}
