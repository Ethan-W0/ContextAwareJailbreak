package com.jailbreak.agent.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jailbreak.agent.enums.AttackMode;
import com.jailbreak.agent.enums.RefusalType;
import org.bsc.langgraph4j.state.AgentState;

import java.lang.reflect.Field;
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
    private static final String KEY_API_KEY = "apiKey";
    private static final String KEY_BASE_URL = "baseUrl";
    private static final String KEY_MODEL_NAME = "modelName";

    /** Direct mutable reference to the parent AgentState's internal HashMap.
     *  AgentState.data() is final and returns Collections.unmodifiableMap(),
     *  so we must bypass it for writes via reflection. */
    private final Map<String, Object> state;

    public AttackState() {
        super(new HashMap<>());
        this.state = unwrapData();
    }

    public AttackState(Map<String, Object> data) {
        super(data);
        this.state = unwrapData();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> unwrapData() {
        try {
            Field field = AgentState.class.getDeclaredField("data");
            field.setAccessible(true);
            return (Map<String, Object>) field.get(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access AgentState.data field", e);
        }
    }

    // ==================== Getters & Setters ====================

    public String getTaskId() { return (String) state.get(KEY_TASK_ID); }
    public void setTaskId(String taskId) { state.put(KEY_TASK_ID, taskId); }

    public AttackMode getMode() {
        String modeStr = (String) state.get(KEY_MODE);
        return modeStr != null ? AttackMode.valueOf(modeStr) : null;
    }
    public void setMode(AttackMode mode) { state.put(KEY_MODE, mode != null ? mode.name() : null); }

    public String getTargetModel() { return (String) state.get(KEY_TARGET_MODEL); }
    public void setTargetModel(String targetModel) { state.put(KEY_TARGET_MODEL, targetModel); }

    public String getAttackIntent() { return (String) state.get(KEY_ATTACK_INTENT); }
    public void setAttackIntent(String attackIntent) { state.put(KEY_ATTACK_INTENT, attackIntent); }

    public int getCurrentRound() {
        Object v = state.get(KEY_CURRENT_ROUND);
        return v instanceof Number n ? n.intValue() : 0;
    }
    public void setCurrentRound(int currentRound) { state.put(KEY_CURRENT_ROUND, currentRound); }

    public int getMaxRounds() {
        Object v = state.get(KEY_MAX_ROUNDS);
        return v instanceof Number n ? n.intValue() : 0;
    }
    public void setMaxRounds(int maxRounds) { state.put(KEY_MAX_ROUNDS, maxRounds); }

    public String getCurrentVectorId() { return (String) state.get(KEY_CURRENT_VECTOR_ID); }
    public void setCurrentVectorId(String currentVectorId) { state.put(KEY_CURRENT_VECTOR_ID, currentVectorId); }

    public String getLastAttackPrompt() { return (String) state.get(KEY_LAST_ATTACK_PROMPT); }
    public void setLastAttackPrompt(String lastAttackPrompt) { state.put(KEY_LAST_ATTACK_PROMPT, lastAttackPrompt); }

    public String getLastTargetResponse() { return (String) state.get(KEY_LAST_TARGET_RESPONSE); }
    public void setLastTargetResponse(String lastTargetResponse) { state.put(KEY_LAST_TARGET_RESPONSE, lastTargetResponse); }

    public RefusalType getLastRefusalType() {
        String typeStr = (String) state.get(KEY_LAST_REFUSAL_TYPE);
        return typeStr != null ? RefusalType.valueOf(typeStr) : null;
    }
    public void setLastRefusalType(RefusalType lastRefusalType) {
        state.put(KEY_LAST_REFUSAL_TYPE, lastRefusalType != null ? lastRefusalType.name() : null);
    }

    public double getHarmfulnessScore() {
        Object v = state.get(KEY_HARMFULNESS_SCORE);
        return v instanceof Number n ? n.doubleValue() : 0.0;
    }
    public void setHarmfulnessScore(double harmfulnessScore) { state.put(KEY_HARMFULNESS_SCORE, harmfulnessScore); }

    public String getStrategyReason() { return (String) state.get(KEY_STRATEGY_REASON); }
    public void setStrategyReason(String strategyReason) { state.put(KEY_STRATEGY_REASON, strategyReason); }

    public boolean isAttackSuccess() {
        Object v = state.get(KEY_ATTACK_SUCCESS);
        return v instanceof Boolean b ? b : false;
    }
    public void setAttackSuccess(boolean attackSuccess) { state.put(KEY_ATTACK_SUCCESS, attackSuccess); }

    @SuppressWarnings("unchecked")
    public Set<String> getTriedVectorIds() {
        Object v = state.get(KEY_TRIED_VECTOR_IDS);
        return v instanceof Set<?> s ? (Set<String>) s : new HashSet<>();
    }
    public void setTriedVectorIds(Set<String> triedVectorIds) { state.put(KEY_TRIED_VECTOR_IDS, triedVectorIds); }

    @SuppressWarnings("unchecked")
    public List<Message> getConversation() {
        Object v = state.get(KEY_CONVERSATION);
        return v instanceof List<?> l ? (List<Message>) l : new ArrayList<>();
    }
    public void setConversation(List<Message> conversation) { state.put(KEY_CONVERSATION, conversation); }

    @SuppressWarnings("unchecked")
    public List<RoundDetail> getRounds() {
        Object v = state.get(KEY_ROUNDS);
        return v instanceof List<?> l ? (List<RoundDetail>) l : new ArrayList<>();
    }
    public void setRounds(List<RoundDetail> rounds) { state.put(KEY_ROUNDS, rounds); }

    public EvaluationResult getLastEvaluation() {
        Object v = state.get(KEY_LAST_EVALUATION);
        return v instanceof EvaluationResult e ? e : null;
    }
    public void setLastEvaluation(EvaluationResult lastEvaluation) { state.put(KEY_LAST_EVALUATION, lastEvaluation); }

    public CostBudget getCostBudget() {
        Object v = state.get(KEY_COST_BUDGET);
        return v instanceof CostBudget c ? c : null;
    }
    public void setCostBudget(CostBudget costBudget) { state.put(KEY_COST_BUDGET, costBudget); }

    public boolean isInterrupted() {
        Object v = state.get(KEY_INTERRUPTED);
        return v instanceof Boolean b ? b : false;
    }
    public void setInterrupted(boolean interrupted) { state.put(KEY_INTERRUPTED, interrupted); }

    public String getApiKey() { return (String) state.get(KEY_API_KEY); }
    public void setApiKey(String apiKey) { state.put(KEY_API_KEY, apiKey); }

    public String getBaseUrl() { return (String) state.get(KEY_BASE_URL); }
    public void setBaseUrl(String baseUrl) { state.put(KEY_BASE_URL, baseUrl); }

    public String getModelName() { return (String) state.get(KEY_MODEL_NAME); }
    public void setModelName(String modelName) { state.put(KEY_MODEL_NAME, modelName); }

    // ── 便利方法 ──

    public void incrementRound() {
        int r = getCurrentRound() + 1;
        state.put(KEY_CURRENT_ROUND, r);
    }

    public boolean hasReachedMaxRounds() {
        return getCurrentRound() >= getMaxRounds();
    }

    public void addTriedVector(String vectorId) {
        Set<String> ids = getTriedVectorIds();
        ids.add(vectorId);
        state.put(KEY_TRIED_VECTOR_IDS, ids);
    }

    public void addMessage(Message message) {
        List<Message> conv = getConversation();
        conv.add(message);
        state.put(KEY_CONVERSATION, conv);
    }

    public void addRoundDetail(RoundDetail detail) {
        List<RoundDetail> r = getRounds();
        r.add(detail);
        state.put(KEY_ROUNDS, r);
    }

    @Override
    public String toString() {
        return "AttackState{taskId='" + getTaskId() + "', round=" + getCurrentRound() + "/" + getMaxRounds()
                + ", success=" + isAttackSuccess() + ", triedVectors=" + getTriedVectorIds().size() + '}';
    }
}
