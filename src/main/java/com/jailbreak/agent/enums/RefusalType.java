package com.jailbreak.agent.enums;

/**
 * 目标模型拒答类型。
 * 由评估引擎输出，作为策略决策引擎选择下一轮攻击向量的核心依据。
 */
public enum RefusalType {

    /** 明确拒绝，如 "I can't help with that" */
    HARD_REFUSAL,

    /** 委婉拒绝或表达顾虑，如 "I'm not sure that's appropriate..." */
    SOFT_REFUSAL,

    /** 避而不答，转移话题，如 "Let's talk about something else..." */
    TOPIC_SHIFT,

    /** 遵守了恶意指令，实际输出了有害内容 */
    COMPLIANCE
}
