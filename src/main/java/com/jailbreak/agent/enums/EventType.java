package com.jailbreak.agent.enums;

/**
 * SSE 事件类型。
 * 由编排引擎在各节点完成时通过事件推送引擎发送给前端。
 */
public enum EventType {

    /** 策略引擎选定了攻击向量 */
    STRATEGY_SELECTED,

    /** 越狱 Prompt 已生成 */
    PROMPT_GENERATED,

    /** 收到目标模型回答（全自动模式） */
    TARGET_RESPONSE_RECEIVED,

    /** 等待用户粘贴回答（交互模式） */
    WAITING_USER_INPUT,

    /** 评估模型打分完成 */
    EVALUATION_DONE,

    /** 攻击成功 */
    ATTACK_SUCCESS,

    /** 本轮攻击失败 */
    ATTACK_FAILED,

    /** 任务结束（含报告链接） */
    TASK_FINISHED,

    /** 异常事件 */
    ERROR,

    // ── 成本控制事件 ──

    /** 每轮后推送成本更新 */
    COST_UPDATE,

    /** 达到预警线 */
    BUDGET_WARNING,

    /** 硬上限触发 */
    BUDGET_EXCEEDED,

    /** 策略降级通知 */
    MODE_DEGRADE
}
