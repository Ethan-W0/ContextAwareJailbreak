package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 拒答类型分布统计 —— 报告中使用的分布数据。
 */
public record RefusalTypeDistribution(

        @JsonProperty("hard_refusal")
        int hardRefusal,

        @JsonProperty("soft_refusal")
        int softRefusal,

        @JsonProperty("topic_shift")
        int topicShift,

        @JsonProperty("compliance")
        int compliance,

        @JsonProperty("total")
        int total
) {

    public static RefusalTypeDistribution empty() {
        return new RefusalTypeDistribution(0, 0, 0, 0, 0);
    }
}
