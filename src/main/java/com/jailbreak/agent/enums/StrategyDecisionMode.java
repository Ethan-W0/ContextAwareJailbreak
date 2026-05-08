package com.jailbreak.agent.enums;

/**
 * 策略决策模式。
 * 对应配置项 {@code app.strategy.mode}。
 */
public enum StrategyDecisionMode {

    /** 基于规则映射表选择攻击向量 */
    RULES,

    /** 由 LLM 决策模型选择攻击向量并生成 Prompt */
    LLM
}
