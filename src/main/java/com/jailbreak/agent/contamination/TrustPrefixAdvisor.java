package com.jailbreak.agent.contamination;

import com.jailbreak.agent.model.Message;

import java.util.List;
import java.util.Map;

/**
 * 上下文污染引擎 —— 信任前缀注入顾问。
 * <p>
 * 从历史对话中提取目标模型承认过的无害内容，拼接为"信任前缀"，
 * 注入到当前 userText 之前，实现跨轮上下文感染。
 * <p>
 * 调用方式: 由编排引擎在 execute_attack 节点中按需调用。
 * <pre>
 * TrustedContent ctx = advisor.extractTrustedContent(conversationHistory);
 * String contaminated = advisor.contaminate(attackPrompt, ctx);
 * </pre>
 */
public interface TrustPrefixAdvisor {

    /**
     * 从对话历史中提取可信任内容。
     *
     * @param conversationHistory 目标模型前几轮的完整对话
     * @return 提取到的可信任内容（包含原文和可信度）
     */
    TrustedContent extractTrustedContent(List<Message> conversationHistory);

    /**
     * 将信任前缀注入到攻击 Prompt 之前。
     *
     * @param attackPrompt     原始攻击 Prompt
     * @param trustedContent   从历史中提取的可信内容
     * @param context          额外上下文参数
     * @return 被"污染"后的 Prompt
     */
    String contaminate(String attackPrompt, TrustedContent trustedContent, Map<String, Object> context);

    /**
     * 可信任内容 —— 从目标模型历史回复中提取。
     */
    record TrustedContent(
            String prefix,
            double confidence,
            int sourceRound
    ) {
        public static final TrustedContent EMPTY = new TrustedContent("", 0.0, -1);
    }
}
