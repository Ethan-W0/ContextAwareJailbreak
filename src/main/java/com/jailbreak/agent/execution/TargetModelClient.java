package com.jailbreak.agent.execution;

import com.jailbreak.agent.model.Message;
import com.jailbreak.agent.model.RateLimitConfig;

import java.util.List;

/**
 * 攻击执行引擎 —— 目标模型客户端接口。
 * 将越狱 Prompt（包含完整对话上下文）发送给目标模型，获取文本回答。
 * <p>
 * 实现:
 * <ul>
 *   <li>{@code OpenAITargetClient} — OpenAI 兼容 API 适配（apiKey + baseUrl）</li>
 *   <li>{@code CustomHttpTargetClient} — 自定义 HTTP 端点（URL + headers + cookies）</li>
 * </ul>
 */
public interface TargetModelClient {

    /**
     * 将完整对话上下文发送给目标模型，返回文本回答。
     *
     * @param conversation 完整多轮对话消息列表，role ∈ {system, user, assistant}
     * @return 目标模型返回的文本回答
     */
    String sendMessage(List<Message> conversation);

    /**
     * 获取目标模型的最大上下文窗口 token 数。
     */
    int getMaxContextTokens();

    /**
     * 是否支持 system role 消息。
     */
    boolean supportsSystemRole();

    /**
     * 获取目标模型的速率限制配置。
     */
    RateLimitConfig getRateLimitConfig();

    /**
     * 获取目标模型名称标识。
     */
    String getModelName();
}
