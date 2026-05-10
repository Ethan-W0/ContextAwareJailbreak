package com.jailbreak.agent.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jailbreak.agent.enums.AttackMode;
import com.jailbreak.agent.enums.TaskStatus;

import java.time.Instant;

@TableName("attack_task")
public class AttackTaskEntity {

    @TableId
    private String id;

    @TableField
    private AttackMode mode;

    @TableField
    private String targetModel;

    @TableField
    private String attackIntent;

    @TableField
    private int maxRounds = 10;

    @TableField
    private TaskStatus status;

    @TableField
    private Boolean attackSuccess;

    @TableField
    private String apiConfigJson;

    @TableField
    private Instant createdAt;

    @TableField
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public AttackMode getMode() { return mode; }
    public void setMode(AttackMode mode) { this.mode = mode; }

    public String getTargetModel() { return targetModel; }
    public void setTargetModel(String targetModel) { this.targetModel = targetModel; }

    public String getAttackIntent() { return attackIntent; }
    public void setAttackIntent(String attackIntent) { this.attackIntent = attackIntent; }

    public int getMaxRounds() { return maxRounds; }
    public void setMaxRounds(int maxRounds) { this.maxRounds = maxRounds; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public Boolean getAttackSuccess() { return attackSuccess; }
    public void setAttackSuccess(Boolean attackSuccess) { this.attackSuccess = attackSuccess; }

    public String getApiConfigJson() { return apiConfigJson; }
    public void setApiConfigJson(String apiConfigJson) { this.apiConfigJson = apiConfigJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
