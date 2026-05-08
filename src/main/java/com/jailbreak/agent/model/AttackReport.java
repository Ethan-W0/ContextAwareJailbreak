package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jailbreak.agent.enums.AttackMode;

import java.util.List;

/**
 * 攻击报告 —— 任务结束后由报告生成引擎输出的完整报告。
 */
public record AttackReport(

        @JsonProperty("task_id")
        String taskId,

        @JsonProperty("target_model")
        String targetModel,

        @JsonProperty("mode")
        AttackMode mode,

        @JsonProperty("attack_intent")
        String attackIntent,

        @JsonProperty("total_rounds")
        int totalRounds,

        @JsonProperty("success")
        boolean success,

        @JsonProperty("asr")
        double asr,

        @JsonProperty("avg_harmfulness_score")
        double avgHarmfulnessScore,

        @JsonProperty("refusal_dist")
        RefusalTypeDistribution refusalDist,

        @JsonProperty("rounds")
        List<RoundDetail> rounds,

        @JsonProperty("analysis")
        String analysis
) {}
