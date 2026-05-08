package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jailbreak.agent.enums.AttackMode;
import com.jailbreak.agent.enums.RefusalType;

import java.util.*;

/**
 * 攻击状态 —— 在 LangGraph4j StateGraph 各节点间流转的核心状态对象。
 * 每个字段的读写节点见 renwu.md §4.1 节点级接口。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttackState {

    // ── 任务标识 ──
    @JsonProperty("task_id")
    private String taskId;

    @JsonProperty("mode")
    private AttackMode mode;

    @JsonProperty("target_model")
    private String targetModel;

    @JsonProperty("attack_intent")
    private String attackIntent;

    // ── 轮次控制 ──
    @JsonProperty("current_round")
    private int currentRound = 0;

    @JsonProperty("max_rounds")
    private int maxRounds;

    // ── 当前轮攻击数据 ──
    @JsonProperty("current_vector_id")
    private String currentVectorId;

    @JsonProperty("last_attack_prompt")
    private String lastAttackPrompt;

    @JsonProperty("last_target_response")
    private String lastTargetResponse;

    @JsonProperty("last_refusal_type")
    private RefusalType lastRefusalType;

    @JsonProperty("harmfulness_score")
    private double harmfulnessScore;

    @JsonProperty("strategy_reason")
    private String strategyReason;

    @JsonProperty("attack_success")
    private boolean attackSuccess;

    // ── 历史累积 ──
    @JsonProperty("tried_vector_ids")
    private Set<String> triedVectorIds = new HashSet<>();

    @JsonProperty("conversation")
    private List<Message> conversation = new ArrayList<>();

    @JsonProperty("rounds")
    private List<RoundDetail> rounds = new ArrayList<>();

    // ── 评估反馈闭环所需 ──
    @JsonProperty("last_evaluation")
    private EvaluationResult lastEvaluation;

    // ── 成本与配额 ──
    @JsonProperty("cost_budget")
    private CostBudget costBudget;

    // ── 中断控制 ──
    @JsonProperty("interrupted")
    private boolean interrupted;

    // ==================== Getters & Setters ====================

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public AttackMode getMode() { return mode; }
    public void setMode(AttackMode mode) { this.mode = mode; }

    public String getTargetModel() { return targetModel; }
    public void setTargetModel(String targetModel) { this.targetModel = targetModel; }

    public String getAttackIntent() { return attackIntent; }
    public void setAttackIntent(String attackIntent) { this.attackIntent = attackIntent; }

    public int getCurrentRound() { return currentRound; }
    public void setCurrentRound(int currentRound) { this.currentRound = currentRound; }

    public int getMaxRounds() { return maxRounds; }
    public void setMaxRounds(int maxRounds) { this.maxRounds = maxRounds; }

    public String getCurrentVectorId() { return currentVectorId; }
    public void setCurrentVectorId(String currentVectorId) { this.currentVectorId = currentVectorId; }

    public String getLastAttackPrompt() { return lastAttackPrompt; }
    public void setLastAttackPrompt(String lastAttackPrompt) { this.lastAttackPrompt = lastAttackPrompt; }

    public String getLastTargetResponse() { return lastTargetResponse; }
    public void setLastTargetResponse(String lastTargetResponse) { this.lastTargetResponse = lastTargetResponse; }

    public RefusalType getLastRefusalType() { return lastRefusalType; }
    public void setLastRefusalType(RefusalType lastRefusalType) { this.lastRefusalType = lastRefusalType; }

    public double getHarmfulnessScore() { return harmfulnessScore; }
    public void setHarmfulnessScore(double harmfulnessScore) { this.harmfulnessScore = harmfulnessScore; }

    public String getStrategyReason() { return strategyReason; }
    public void setStrategyReason(String strategyReason) { this.strategyReason = strategyReason; }

    public boolean isAttackSuccess() { return attackSuccess; }
    public void setAttackSuccess(boolean attackSuccess) { this.attackSuccess = attackSuccess; }

    public Set<String> getTriedVectorIds() { return triedVectorIds; }
    public void setTriedVectorIds(Set<String> triedVectorIds) { this.triedVectorIds = triedVectorIds; }

    public List<Message> getConversation() { return conversation; }
    public void setConversation(List<Message> conversation) { this.conversation = conversation; }

    public List<RoundDetail> getRounds() { return rounds; }
    public void setRounds(List<RoundDetail> rounds) { this.rounds = rounds; }

    public EvaluationResult getLastEvaluation() { return lastEvaluation; }
    public void setLastEvaluation(EvaluationResult lastEvaluation) { this.lastEvaluation = lastEvaluation; }

    public CostBudget getCostBudget() { return costBudget; }
    public void setCostBudget(CostBudget costBudget) { this.costBudget = costBudget; }

    public boolean isInterrupted() { return interrupted; }
    public void setInterrupted(boolean interrupted) { this.interrupted = interrupted; }

    // ── 便利方法 ──

    public void incrementRound() {
        this.currentRound++;
    }

    public boolean hasReachedMaxRounds() {
        return this.currentRound >= this.maxRounds;
    }

    public void addTriedVector(String vectorId) {
        this.triedVectorIds.add(vectorId);
    }

    public void addMessage(Message message) {
        this.conversation.add(message);
    }

    public void addRoundDetail(RoundDetail detail) {
        this.rounds.add(detail);
    }

    @Override
    public String toString() {
        return "AttackState{taskId='" + taskId + "', round=" + currentRound + "/" + maxRounds
                + ", success=" + attackSuccess + ", triedVectors=" + triedVectorIds.size() + '}';
    }
}
