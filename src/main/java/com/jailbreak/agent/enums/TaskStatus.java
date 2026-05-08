package com.jailbreak.agent.enums;

/**
 * 任务生命周期状态。
 */
public enum TaskStatus {

    /** 已创建，尚未启动 */
    CREATED,

    /** 正在运行 */
    RUNNING,

    /** 已暂停 */
    PAUSED,

    /** 正常完成 */
    FINISHED,

    /** 用户手动终止 */
    TERMINATED,

    /** 异常中断（可恢复） */
    INTERRUPTED,

    /** 已归档 */
    ARCHIVED
}
