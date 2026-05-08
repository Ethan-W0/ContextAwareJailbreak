package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jailbreak.agent.enums.CallType;

/**
 * API 调用用量记录 —— 每次 LLM API 调用后记录。
 */
public record UsageRecord(

        @JsonProperty("call_type")
        CallType callType,

        @JsonProperty("prompt_tokens")
        long promptTokens,

        @JsonProperty("completion_tokens")
        long completionTokens,

        @JsonProperty("model_name")
        String modelName
) {

    public long totalTokens() {
        return promptTokens + completionTokens;
    }
}
