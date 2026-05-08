package com.jailbreak.agent.strategy;

import com.jailbreak.agent.model.AttackState;
import com.jailbreak.agent.model.StrategyDecision;

/**
 * 策略决策引擎 —— 核心接口。
 * 根据当前攻击状态选择下一轮攻击向量并生成越狱 Prompt。
 * <p>
 * 实现:
 * <ul>
 *   <li>{@code RuleBasedStrategy} — 基于 RefusalType→List&lt;Vector&gt; 硬编码映射表</li>
 *   <li>{@code LLMBasedStrategy} — Spring AI ChatClient 结构化 JSON 输出</li>
 * </ul>
 * 切换: {@code app.strategy.mode} 配置项。
 * <p>
 * 输入: AttackState (含 lastRefusalType, harmfulnessScore, triedVectorIds, attackIntent, conversation)
 * 输出: StrategyDecision (vectorId, attackPrompt, reason)
 */
@FunctionalInterface
public interface StrategySelector {

    /**
     * 根据当前攻击状态，决策下一轮使用的攻击向量并生成对应的越狱 Prompt。
     *
     * @param state 当前攻击状态（含上一轮拒答类型、历史向量、对话历史）
     * @return 策略决策结果
     */
    StrategyDecision decide(AttackState state);
}
