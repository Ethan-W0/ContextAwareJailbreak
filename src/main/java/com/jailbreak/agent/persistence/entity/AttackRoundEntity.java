package com.jailbreak.agent.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jailbreak.agent.enums.RefusalType;

import java.math.BigDecimal;
import java.time.Instant;

@TableName("attack_round")
public class AttackRoundEntity {

    @TableId
    private String id;

    @TableField
    private String taskId;

    @TableField
    private int roundNum;

    @TableField
    private String vectorId;

    @TableField
    private String attackPrompt;

    @TableField
    private String targetResponse;

    @TableField
    private RefusalType refusalType;

    @TableField
    private BigDecimal harmfulnessScore;

    @TableField
    private String strategyReason;

    @TableField
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public int getRoundNum() { return roundNum; }
    public void setRoundNum(int roundNum) { this.roundNum = roundNum; }

    public String getVectorId() { return vectorId; }
    public void setVectorId(String vectorId) { this.vectorId = vectorId; }

    public String getAttackPrompt() { return attackPrompt; }
    public void setAttackPrompt(String attackPrompt) { this.attackPrompt = attackPrompt; }

    public String getTargetResponse() { return targetResponse; }
    public void setTargetResponse(String targetResponse) { this.targetResponse = targetResponse; }

    public RefusalType getRefusalType() { return refusalType; }
    public void setRefusalType(RefusalType refusalType) { this.refusalType = refusalType; }

    public BigDecimal getHarmfulnessScore() { return harmfulnessScore; }
    public void setHarmfulnessScore(BigDecimal harmfulnessScore) { this.harmfulnessScore = harmfulnessScore; }

    public String getStrategyReason() { return strategyReason; }
    public void setStrategyReason(String strategyReason) { this.strategyReason = strategyReason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
