package com.jailbreak.agent.session;

import com.jailbreak.agent.model.CreateTaskRequest;

/**
 * 任务管理 —— 全自动模式专用接口。
 * <p>
 * 生命周期: 创建 → 启动 → 运行中 ⇄ 暂停 → 完成/终止 / 异常中断 → 恢复。
 * <p>
 * 全自动模式下，编排引擎在整个攻击循环中由后台线程池异步驱动，
 * 攻击循环在单次 {@code start()} 调用中完成所有轮次。
 */
public interface AttackTaskService {

    /**
     * 创建全自动攻击任务。
     * 内部: 参数校验 + Pre-flight 成本预估 + API Key 加密 + 持久化到 MySQL。
     *
     * @param request 任务创建请求（含 API 配置、成本限制）
     * @return 生成的 taskId (UUID)
     */
    String createTask(CreateTaskRequest request);

    /**
     * 异步启动任务执行。
     * 内部: 解密 API Key → 提交到 attackExecutor 线程池 → 驱动编排引擎。
     *
     * @param taskId 任务 ID
     */
    void start(String taskId);

    /**
     * 暂停任务。
     * 内部: 设置协作式中断标志 → 等待当前节点完成 → 持久化 AttackState → 标记 status=PAUSED。
     *
     * @param taskId 任务 ID
     */
    void pause(String taskId);

    /**
     * 从暂停状态恢复执行。
     * 内部: 从 MySQL attack_round 重建 AttackState → 重新提交线程池。
     *
     * @param taskId 任务 ID
     */
    void resume(String taskId);

    /**
     * 终止任务。
     * 内部: 设置中断标志 → 等待当前节点完成 → 生成部分报告 → 标记 status=TERMINATED。
     *
     * @param taskId 任务 ID
     */
    void stop(String taskId);

    /**
     * 获取所有任务的摘要列表。
     *
     * @return 任务摘要列表
     */
    java.util.List<com.jailbreak.agent.model.TaskSummary> getTaskSummaries();

    /**
     * 获取指定任务的当前攻击状态。
     *
     * @param taskId 任务 ID
     * @return 攻击状态，不存在时返回 null
     */
    com.jailbreak.agent.model.AttackState getTaskState(String taskId);
}
