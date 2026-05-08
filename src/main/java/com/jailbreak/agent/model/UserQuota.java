package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * 用户账户维度配额 —— 全局配置，管理员可调。
 */
public class UserQuota {

    @JsonProperty("max_concurrent_tasks")
    private int maxConcurrentTasks = 3;

    @JsonProperty("max_daily_tasks")
    private int maxDailyTasks = 20;

    @JsonProperty("max_daily_platform_tokens")
    private long maxDailyPlatformTokens = 5_000_000;

    @JsonProperty("daily_tasks_run")
    private int dailyTasksRun;

    @JsonProperty("daily_tokens_used")
    private long dailyTokensUsed;

    @JsonProperty("reset_at")
    private Instant resetAt;

    // ── Getters & Setters ──

    public int getMaxConcurrentTasks() { return maxConcurrentTasks; }
    public void setMaxConcurrentTasks(int maxConcurrentTasks) { this.maxConcurrentTasks = maxConcurrentTasks; }

    public int getMaxDailyTasks() { return maxDailyTasks; }
    public void setMaxDailyTasks(int maxDailyTasks) { this.maxDailyTasks = maxDailyTasks; }

    public long getMaxDailyPlatformTokens() { return maxDailyPlatformTokens; }
    public void setMaxDailyPlatformTokens(long maxDailyPlatformTokens) { this.maxDailyPlatformTokens = maxDailyPlatformTokens; }

    public int getDailyTasksRun() { return dailyTasksRun; }
    public void setDailyTasksRun(int dailyTasksRun) { this.dailyTasksRun = dailyTasksRun; }

    public long getDailyTokensUsed() { return dailyTokensUsed; }
    public void setDailyTokensUsed(long dailyTokensUsed) { this.dailyTokensUsed = dailyTokensUsed; }

    public Instant getResetAt() { return resetAt; }
    public void setResetAt(Instant resetAt) { this.resetAt = resetAt; }

    public long getRemainingDailyTokens() {
        return maxDailyPlatformTokens - dailyTokensUsed;
    }

    public int getRemainingDailyTasks() {
        return maxDailyTasks - dailyTasksRun;
    }
}
