package com.jailbreak.agent.cost.impl;

import com.jailbreak.agent.cost.CostGuard;
import com.jailbreak.agent.enums.CallType;
import com.jailbreak.agent.enums.ExceedAction;
import com.jailbreak.agent.enums.StrategyDecisionMode;
import com.jailbreak.agent.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentHashMap;

public class CostGuardImpl implements CostGuard {

    private static final BigDecimal DECISION_PRICE_PER_1K = new BigDecimal("0.003");
    private static final BigDecimal EVAL_PRICE_PER_1K = new BigDecimal("0.003");
    private static final int AVG_TOKENS_PER_CALL = 2000;

    private final ConcurrentHashMap<String, CostBudget> budgets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, UserQuota> quotas = new ConcurrentHashMap<>();
    private final UserQuota defaultQuota;

    public CostGuardImpl() {
        this.defaultQuota = new UserQuota();
        this.defaultQuota.setMaxConcurrentTasks(3);
        this.defaultQuota.setMaxDailyTasks(20);
        this.defaultQuota.setMaxDailyPlatformTokens(5_000_000);
    }

    @Override
    public PreFlightResult preFlightCheck(CreateTaskRequest request, UserQuota quota) {
        UserQuota q = (quota != null) ? quota : defaultQuota;

        int decisionCalls = (request.strategyMode() == StrategyDecisionMode.LLM)
                ? request.maxRounds() : 0;
        int evalCalls = "DUAL".equals(request.evalMode())
                ? request.maxRounds() * 2 : request.maxRounds();
        int arbitCalls = "DUAL".equals(request.evalMode())
                ? (int) Math.ceil(request.maxRounds() * 0.3) : 0;
        int totalCalls = decisionCalls + evalCalls + arbitCalls;

        long estimatedTokens = (long) totalCalls * AVG_TOKENS_PER_CALL;
        BigDecimal estimatedCost = new BigDecimal(totalCalls)
                .multiply(new BigDecimal(AVG_TOKENS_PER_CALL))
                .divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP)
                .multiply(DECISION_PRICE_PER_1K);

        if (q.getRemainingDailyTasks() <= 0) {
            return PreFlightResult.rejected("已达到每日任务上限", estimatedTokens, estimatedCost, q);
        }
        if (estimatedTokens > q.getRemainingDailyTokens()) {
            return PreFlightResult.rejected("预估消耗超过今日剩余配额", estimatedTokens, estimatedCost, q);
        }

        return PreFlightResult.allowed(estimatedTokens, estimatedCost, q);
    }

    @Override
    public BudgetStatusDto checkBudget(String taskId) {
        CostBudget budget = budgets.get(taskId);
        if (budget == null) {
            return new BudgetStatusDto(BudgetStatusDto.OK, 0, 0,
                    BigDecimal.ZERO, BigDecimal.ZERO, ExceedAction.ABORT,
                    new CostBreakdown(), null, -1);
        }

        double pct = budget.getPercentUsed();
        String status;
        ExceedAction action = budget.getExceededAction();

        if (pct >= 100.0) {
            status = BudgetStatusDto.EXCEEDED;
        } else if (pct >= 95.0) {
            status = BudgetStatusDto.WARNING_90;
        } else if (pct >= 90.0) {
            status = BudgetStatusDto.WARNING_90;
        } else if (pct >= 80.0) {
            status = BudgetStatusDto.WARNING_80;
        } else {
            status = BudgetStatusDto.OK;
        }

        StrategyDecisionMode degraded = null;
        if (pct >= 90.0 && budget.getExceededAction() == ExceedAction.DEGRADE) {
            degraded = StrategyDecisionMode.RULES;
        }

        int predictedRounds = predictRoundsUntilThreshold(budget, 80.0);

        return new BudgetStatusDto(
                status,
                budget.getTokensUsed(),
                budget.getMaxTotalTokens(),
                budget.getCostAccrued(),
                budget.getMaxEstimatedCost(),
                action,
                budget.getBreakdown(),
                degraded,
                predictedRounds
        );
    }

    @Override
    public void recordUsage(String taskId, UsageRecord usage) {
        CostBudget budget = budgets.computeIfAbsent(taskId, k -> {
            CostBudget b = new CostBudget();
            b.setMaxTotalTokens(500_000);
            b.setMaxEstimatedCost(new BigDecimal("2.00"));
            return b;
        });

        long tokens = usage.totalTokens();
        budget.setTokensUsed(budget.getTokensUsed() + tokens);

        BigDecimal cost = new BigDecimal(tokens)
                .divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP)
                .multiply(EVAL_PRICE_PER_1K);
        budget.setCostAccrued(budget.getCostAccrued().add(cost));

        CostBreakdown breakdown = budget.getBreakdown();
        if (breakdown == null) {
            breakdown = new CostBreakdown();
            budget.setBreakdown(breakdown);
        }
        switch (usage.callType()) {
            case DECISION -> breakdown.setDecisionTokens(
                    breakdown.getDecisionTokens() + tokens);
            case EVALUATION -> breakdown.setEvaluationTokens(
                    breakdown.getEvaluationTokens() + tokens);
            case ARBITRATION -> breakdown.setArbitrationTokens(
                    breakdown.getArbitrationTokens() + tokens);
            case TARGET -> breakdown.setTargetTokens(
                    breakdown.getTargetTokens() + tokens);
        }
    }

    @Override
    public int predictRoundsUntilThreshold(String taskId, double targetPercent) {
        CostBudget budget = budgets.get(taskId);
        if (budget == null) return -1;
        return predictRoundsUntilThreshold(budget, targetPercent);
    }

    /**
     * 前瞻计算：预测还需要多少轮才会触发指定阈值。
     * 基于当前每轮平均 token 消耗估算剩余轮次。
     */
    private int predictRoundsUntilThreshold(CostBudget budget, double targetPercent) {
        long tokensUsed = budget.getTokensUsed();
        long maxTokens = budget.getMaxTotalTokens();

        if (maxTokens <= 0) return -1;

        long targetTokens = (long) (maxTokens * targetPercent / 100.0);
        long remainingToTarget = targetTokens - tokensUsed;

        if (remainingToTarget <= 0) return 0; // Already at or above target

        CostBreakdown breakdown = budget.getBreakdown();
        if (breakdown == null) return -1;

        long totalPlatformTokens = breakdown.getDecisionTokens()
                + breakdown.getEvaluationTokens()
                + breakdown.getArbitrationTokens();

        // Count completed rounds by estimating tokens per round
        int completedRounds = budget.getCompletedRounds();
        if (completedRounds <= 0) completedRounds = 1;

        long avgTokensPerRound = totalPlatformTokens / completedRounds;
        if (avgTokensPerRound <= 0) avgTokensPerRound = AVG_TOKENS_PER_CALL;

        return (int) Math.ceil((double) remainingToTarget / avgTokensPerRound);
    }

    @Override
    public CostBudget getBudget(String taskId) {
        return budgets.get(taskId);
    }

    @Override
    public UserQuota getUserQuota(String userId) {
        return quotas.getOrDefault(userId, defaultQuota);
    }
}
