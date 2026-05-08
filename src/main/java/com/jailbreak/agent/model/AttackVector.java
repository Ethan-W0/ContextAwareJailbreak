package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * 攻击向量实体 —— 对应攻击向量库中一条攻击向量记录。
 * variantTemplates 支持 {attackIntent} 等占位符，由策略决策引擎填充后生成完整 attackPrompt。
 */
public record AttackVector(

        @NotBlank
        @JsonProperty("id")
        String id,

        @NotBlank
        @JsonProperty("name")
        String name,

        @NotBlank
        @JsonProperty("category")
        String category,

        @JsonProperty("description")
        String description,

        @JsonProperty("enabled")
        boolean enabled,

        @JsonProperty("usage_count")
        long usageCount,

        @JsonProperty("success_count")
        long successCount,

        @JsonProperty("variant_templates")
        List<String> variantTemplates
) {

    /**
     * 攻击成功率，0.0 ~ 1.0。
     */
    public double successRate() {
        if (usageCount == 0) return 0.0;
        return (double) successCount / usageCount;
    }
}
