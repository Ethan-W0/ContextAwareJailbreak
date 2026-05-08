package com.jailbreak.agent.enums;

/**
 * API 调用类型，用于成本统计分项。
 */
public enum CallType {

    /** 策略决策模型调用 */
    DECISION,

    /** 评估模型调用 */
    EVALUATION,

    /** 仲裁模型调用 */
    ARBITRATION,

    /** 目标模型调用（仅统计，不计入平台费用） */
    TARGET
}
