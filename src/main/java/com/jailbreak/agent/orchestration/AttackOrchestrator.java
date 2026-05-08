package com.jailbreak.agent.orchestration;

import com.jailbreak.agent.model.AttackState;
import com.jailbreak.agent.model.ExecutionEvent;

import java.util.function.Consumer;

/**
 * 攻击编排引擎 —— 系统核心协调者。
 * 基于 LangGraph4j StateGraph 驱动攻击状态机，协调各模块按序执行。
 * <p>
 * 所有业务模块通过此引擎间接调用，彼此不直接耦合。
 * 每个节点完成后通过回调向事件推送引擎发出 {@link ExecutionEvent}。
 * <p>
 * 状态机节点流转:
 * <pre>
 * [START] → select_strategy → execute_attack → evaluate_response → route
 *              ↑                                                    │
 *              └──────────── continue ──────────────────────────────┘
 *                                                                   │
 *                                                          finish → [END]
 * </pre>
 */
public interface AttackOrchestrator {

    /**
     * 同步运行攻击编排（用于交互模式）。
     * 推进一个节点后挂起，等待下次调用。
     *
     * @param state       当前攻击状态
     * @param eventSink   事件回调（每个节点完成后推送给事件推送引擎）
     * @return 更新后的攻击状态
     */
    AttackState runStep(AttackState state, Consumer<ExecutionEvent> eventSink);

    /**
     * 异步运行完整攻击循环（用于全自动模式）。
     * 内部提交到线程池执行，通过 eventSink 实时推送事件。
     *
     * @param state       初始攻击状态
     * @param eventSink   事件回调
     */
    void runFull(AttackState state, Consumer<ExecutionEvent> eventSink);

    /**
     * 请求中断当前运行中的编排。
     * 设置协作式中断标志，当前节点完成后安全停止。
     *
     * @param taskId 任务 ID
     */
    void interrupt(String taskId);

    /**
     * 检查指定任务是否正在运行。
     */
    boolean isRunning(String taskId);
}
