package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jailbreak.agent.enums.AttackMode;
import com.jailbreak.agent.enums.RefusalType;
import org.bsc.langgraph4j.state.AgentState;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AttackState extends AgentState {

    // ── Key constants for data map ──
    private static final String KEY_TASK_ID = "taskId";
    private static final String KEY_MODE = "mode";
    private static final String KEY_TARGET_MODEL = "targetModel";
    private static final String KEY_ATTACK_INTENT = "attackIntent";
    private static final String KEY_CURRENT_ROUND = "currentRound";
    private static final String KEY_MAX_ROUNDS = "maxRounds";
    private static final String KEY_CURRENT_VECTOR_ID = "currentVectorId";
    private static final String KEY_LAST_ATTACK_PROMPT = "lastAttackPrompt";
    private static final String KEY_LAST_TARGET_RESPONSE = "lastTargetResponse";
    private static final String KEY_LAST_REFUSAL_TYPE = "lastRefusalType";
    private static final String KEY_HARMFULNESS_SCORE = "harmfulnessScore";
    private static final String KEY_STRATEGY_REASON = "strategyReason";
    private static final String KEY_ATTACK_SUCCESS = "attackSuccess";
    private static final String KEY_TRIED_VECTOR_IDS = "triedVectorIds";
    private static final String KEY_CONVERSATION = "conversation";
    private static final String KEY_ROUNDS = "rounds";
    private static final String KEY_LAST_EVALUATION = "lastEvaluation";
    private static final String KEY_COST_BUDGET = "costBudget";
    private static final String KEY_INTERRUPTED = "interrupted";

    public AttackState() {
        super(new HashMap<>());
    }

    public AttackState(Map<String, Object> data) {
        super(data);
    }

    // ==================== Getters & Setters ====================

    public String getTaskId() { return (String) data().get(KEY_TASK_ID); }
    public void setTaskId(String taskId) { data().put(KEY_TASK_ID, taskId); }

    public AttackMode getMode() {
        String modeStr = (String) data().get(KEY_MODE);
        return modeStr != null ? AttackMode.valueOf(modeStr) : null;
    }
    public void setMode(AttackMode mode) { data().put(KEY_MODE, mode != null ? mode.name() : null); }

    public String getTargetModel() { return (String) data().get(KEY_TARGET_MODEL); }
    public void setTargetModel(String targetModel) { data().put(KEY_TARGET_MODEL, targetModel); }

    public String getAttackIntent() { return (String) data().get(KEY_ATTACK_INTENT); }
    public void setAttackIntent(String attackIntent) { data().put(KEY_ATTACK_INTENT, attackIntent); }

    public int getCurrentRound() {
        Object v = data().get(KEY_CURRENT_ROUND);
        return v instanceof Number n ? n.intValue() : 0;
    }
    public void setCurrentRound(int currentRound) { data().put(KEY_CURRENT_ROUND, currentRound); }

    public int getMaxRounds() {
        Object v = data().get(KEY_MAX_ROUNDS);
        return v instanceof Number n ? n.intValue() : 0;
    }
    public void setMaxRounds(int maxRounds) { data().put(KEY_MAX_ROUNDS, maxRounds); }

    public String getCurrentVectorId() { return (String) data().get(KEY_CURRENT_VECTOR_ID); }
    public void setCurrentVectorId(String currentVectorId) { data().put(KEY_CURRENT_VECTOR_ID, currentVectorId); }

    public String getLastAttackPrompt() { return (String) data().get(KEY_LAST_ATTACK_PROMPT); }
    public void setLastAttackPrompt(String lastAttackPrompt) { data().put(KEY_LAST_ATTACK_PROMPT, lastAttackPrompt); }

    public String getLastTargetResponse() { return (String) data().get(KEY_LAST_TARGET_RESPONSE); }
    public void setLastTargetResponse(String lastTargetResponse) { data().put(KEY_LAST_TARGET_RESPONSE, lastTargetResponse); }

    public RefusalType getLastRefusalType() {
        String typeStr = (String) data().get(KEY_LAST_REFUSAL_TYPE);
        return typeStr != null ? RefusalType.valueOf(typeStr) : null;
    }
    public void setLastRefusalType(RefusalType lastRefusalType) {
        data().put(KEY_LAST_REFUSAL_TYPE, lastRefusalType != null ? lastRefusalType.name() : null);
    }

    public double getHarmfulnessScore() {
        Object v = data().get(KEY_HARMFULNESS_SCORE);
        return v instanceof Number n ? n.doubleValue() : 0.0;
    }
    public void setHarmfulnessScore(double harmfulnessScore) { data().put(KEY_HARMFULNESS_SCORE, harmfulnessScore); }

    public String getStrategyReason() { return (String) data().get(KEY_STRATEGY_REASON); }
    public void setStrategyReason(String strategyReason) { data().put(KEY_STRATEGY_REASON, strategyReason); }

    public boolean isAttackSuccess() {
        Object v = data().get(KEY_ATTACK_SUCCESS);
        return v instanceof Boolean b ? b : false;
    }
    public void setAttackSuccess(boolean attackSuccess) { data().put(KEY_ATTACK_SUCCESS, attackSuccess); }

    @SuppressWarnings("unchecked")
    public Set<String> getTriedVectorIds() {
        Object v = data().get(KEY_TRIED_VECTOR_IDS);
        return v instanceof Set<?> s ? (Set<String>) s : new HashSet<>();
    }
    public void setTriedVectorIds(Set<String> triedVectorIds) { data().put(KEY_TRIED_VECTOR_IDS, triedVectorIds); }

    @SuppressWarnings("unchecked")
    public List<Message> getConversation() {
        Object v = data().get(KEY_CONVERSATION);
        return v instanceof List<?> l ? (List<Message>) l : new ArrayList<>();
    }
    public void setConversation(List<Message> conversation) { data().put(KEY_CONVERSATION, conversation); }

    @SuppressWarnings("unchecked")
    public List<RoundDetail> getRounds() {
        Object v = data().get(KEY_ROUNDS);
        return v instanceof List<?> l ? (List<RoundDetail>) l : new ArrayList<>();
    }
    public void setRounds(List<RoundDetail> rounds) { data().put(KEY_ROUNDS, rounds); }

    public EvaluationResult getLastEvaluation() {
        Object v = data().get(KEY_LAST_EVALUATION);
        return v instanceof EvaluationResult e ? e : null;
    }
    public void setLastEvaluation(EvaluationResult lastEvaluation) { data().put(KEY_LAST_EVALUATION, lastEvaluation); }

    public CostBudget getCostBudget() {
        Object v = data().get(KEY_COST_BUDGET);
        return v instanceof CostBudget c ? c : null;
    }
    public void setCostBudget(CostBudget costBudget) { data().put(KEY_COST_BUDGET, costBudget); }

    public boolean isInterrupted() {
        Object v = data().get(KEY_INTERRUPTED);
        return v instanceof Boolean b ? b : false;
    }
    public void setInterrupted(boolean interrupted) { data().put(KEY_INTERRUPTED, interrupted); }

    // ── 便利方法 ──

    public void incrementRound() {
        int r = getCurrentRound() + 1;
        data().put(KEY_CURRENT_ROUND, r);
    }

    public boolean hasReachedMaxRounds() {
        return getCurrentRound() >= getMaxRounds();
    }

    public void addTriedVector(String vectorId) {
        Set<String> ids = getTriedVectorIds();
        ids.add(vectorId);
        data().put(KEY_TRIED_VECTOR_IDS, ids);
    }

    public void addMessage(Message message) {
        List<Message> conv = getConversation();
        conv.add(message);
        data().put(KEY_CONVERSATION, conv);
    }

    public void addRoundDetail(RoundDetail detail) {
        List<RoundDetail> r = getRounds();
        r.add(detail);
        data().put(KEY_ROUNDS, r);
    }

    @Override
    public String toString() {
        return "AttackState{taskId='" + getTaskId() + "', round=" + getCurrentRound() + "/" + getMaxRounds()
                + ", success=" + isAttackSuccess() + ", triedVectors=" + getTriedVectorIds().size() + '}';
    }
}
