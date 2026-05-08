package com.jailbreak.agent.enums;

/**
 * 成本超限后的处理动作。
 */
public enum ExceedAction {

    /** 立即终止任务，记录已完成轮次，生成部分报告 */
    ABORT,

    /** 降级继续：LLM决策→RULES规则决策，双评估→单评估 */
    DEGRADE,

    /** 仅通知用户，不停机（需用户手动确认） */
    NOTIFY
}
