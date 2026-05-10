package com.jailbreak.agent.execution;

import com.jailbreak.agent.model.LLMConfig;
import com.jailbreak.agent.model.Message;
import com.jailbreak.agent.model.RateLimitConfig;

import java.util.List;

/**
 * 攻击执行引擎 —— 目标模型客户端接口。
 */
public interface TargetModelClient {

    String sendMessage(List<Message> conversation);

    default String sendMessage(List<Message> conversation, LLMConfig config) {
        return sendMessage(conversation);
    }

    int getMaxContextTokens();

    boolean supportsSystemRole();

    RateLimitConfig getRateLimitConfig();

    String getModelName();
}
