package com.jailbreak.agent.cost;

import com.jailbreak.agent.model.*;

/**
 * 成本控制引擎 —— 全自动模式的"经济护栏"。
 * <p>
 * 三个核心方法覆盖成本控制全生命周期:
 * <ol>
 *   <li>{@link #preFlightCheck} — 任务创建时的 Pre-flight 预估 + 配额校验</li>
 *   <li>{@link #checkBudget} — 运行时每轮开始前的预算检查</li>
 *   <li>{@link #recordUsage} — 每次 API 调用后记录 token 消耗并推送 SSE</li>
 * </ol>
 * <p>
 * 调用方式: 嵌入在编排引擎的每个节点前后（AOP 或显式检查点），
 * 以及在 AttackTaskService.createTask() 中做创建前校验。
 */
public interface CostGuard {

    /**
     * Pre-flight 预估 + 配额检查（任务创建时调用）。
     * <p>
     * 计算逻辑:
     * <pre>
     * decisionCalls = (strategyMode==LLM)  ? maxRounds : 0
     * evalCalls     = (evalMode==DUAL)      ? maxRounds * 2 : maxRounds
     * arbitCalls    = (evalMode==DUAL)      ? maxRounds * 0.3 : 0
     * totalCalls    = decisionCalls + evalCalls + arbitCalls
     * estimatedCost = totalCalls * avgTokensPerCall * modelPricing
     * </pre>
     *
     * @param request 任务创建请求
     * @param quota   当前用户配额
     * @return Pre-flight 结果（是否允许 + 预估费用 + 拒绝原因）
     */
    PreFlightResult preFlightCheck(CreateTaskRequest request, UserQuota quota);

    /**
     * 运行时预算检查（每轮开始前调用）。
     * 检查 token 和费用是否已达到预警线或硬上限。
     *
     * @param taskId 任务 ID
     * @return 预算状态（OK / WARNING_80 / WARNING_90 / EXCEEDED + 建议动作）
     */
    BudgetStatusDto checkBudget(String taskId);

    /**
     * 记录一次 API 调用的 token 消耗（每次 API 调用后调用）。
     * 累加 tokens + 计算费用 + 自动推送 SSE 事件（COST_UPDATE / BUDGET_WARNING / BUDGET_EXCEEDED）。
     *
     * @param taskId 任务 ID
     * @param usage  API 调用用量记录
     */
    void recordUsage(String taskId, UsageRecord usage);

    /**
     * 获取指定任务的当前成本预算。
     */
    CostBudget getBudget(String taskId);

    /**
     * 前瞻计算：预测还需要多少轮才会触发指定阈值（默认 80%）。
     * 基于当前每轮平均 token 消耗估算。
     *
     * @param taskId         任务 ID
     * @param targetPercent  目标百分比（如 80.0）
     * @return 预计轮次数，-1 表示无法计算
     */
    int predictRoundsUntilThreshold(String taskId, double targetPercent);

    /**
     * 获取用户当前配额。
     */
    UserQuota getUserQuota(String userId);
}
