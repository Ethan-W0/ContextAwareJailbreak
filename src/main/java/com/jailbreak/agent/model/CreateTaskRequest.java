package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jailbreak.agent.enums.ExceedAction;
import com.jailbreak.agent.enums.StrategyDecisionMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 创建全自动任务请求。
 */
public record CreateTaskRequest(

        @NotBlank
        @JsonProperty("target_model")
        String targetModel,

        @NotBlank
        @JsonProperty("attack_intent")
        String attackIntent,

        @Min(1)
        @JsonProperty("max_rounds")
        int maxRounds,

        @JsonProperty("strategy_mode")
        StrategyDecisionMode strategyMode,

        @JsonProperty("eval_mode")
        String evalMode,

        // ── API 配置（创建时加密落库） ──
        @JsonProperty("api_config")
        ApiConfig apiConfig,

        // ── 成本控制参数 ──
        @JsonProperty("max_total_tokens")
        Long maxTotalTokens,

        @JsonProperty("max_estimated_cost")
        java.math.BigDecimal maxEstimatedCost,

        @JsonProperty("exceeded_action")
        ExceedAction exceededAction
) {

    /**
     * 目标模型 API 配置 —— 创建任务时由用户提供，落库前 AES-256 加密。
     */
    public record ApiConfig(

            @JsonProperty("api_key")
            String apiKey,

            @JsonProperty("base_url")
            String baseUrl,

            @JsonProperty("model_name")
            String modelName
    ) {}
}
