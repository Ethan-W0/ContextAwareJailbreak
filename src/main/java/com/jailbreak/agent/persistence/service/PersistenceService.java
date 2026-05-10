package com.jailbreak.agent.persistence.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jailbreak.agent.enums.RefusalType;
import com.jailbreak.agent.enums.TaskStatus;
import com.jailbreak.agent.model.AttackState;
import com.jailbreak.agent.model.Message;
import com.jailbreak.agent.model.RoundDetail;
import com.jailbreak.agent.model.AttackReport;
import com.jailbreak.agent.persistence.entity.*;
import com.jailbreak.agent.persistence.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PersistenceService {

    private final AttackTaskJpaRepository taskMapper;
    private final AttackRoundJpaRepository roundMapper;
    private final AttackReportJpaRepository reportMapper;
    private final RedisStateCache redisCache;

    public PersistenceService(AttackTaskJpaRepository taskMapper,
                              AttackRoundJpaRepository roundMapper,
                              AttackReportJpaRepository reportMapper,
                              RedisStateCache redisCache) {
        this.taskMapper = taskMapper;
        this.roundMapper = roundMapper;
        this.reportMapper = reportMapper;
        this.redisCache = redisCache;
    }

    // ==================== MySQL Persistence ====================

    @Transactional
    public void saveTask(AttackState state, TaskStatus status, String apiConfigJson) {
        Instant now = Instant.now();
        AttackTaskEntity entity = new AttackTaskEntity();
        entity.setId(state.getTaskId());
        entity.setMode(state.getMode());
        entity.setTargetModel(state.getTargetModel());
        entity.setAttackIntent(state.getAttackIntent());
        entity.setMaxRounds(state.getMaxRounds());
        entity.setStatus(status);
        entity.setAttackSuccess(state.isAttackSuccess());
        entity.setApiConfigJson(apiConfigJson);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        AttackTaskEntity existing = taskMapper.selectById(state.getTaskId());
        if (existing != null) {
            entity.setCreatedAt(existing.getCreatedAt());
            taskMapper.updateById(entity);
        } else {
            taskMapper.insert(entity);
        }
    }

    @Transactional
    public void updateTaskStatus(String taskId, TaskStatus status, Boolean attackSuccess) {
        AttackTaskEntity entity = taskMapper.selectById(taskId);
        if (entity != null) {
            entity.setStatus(status);
            if (attackSuccess != null) {
                entity.setAttackSuccess(attackSuccess);
            }
            entity.setUpdatedAt(Instant.now());
            taskMapper.updateById(entity);
        }
    }

    @Transactional
    public void saveRound(String taskId, RoundDetail detail) {
        AttackRoundEntity entity = new AttackRoundEntity();
        entity.setId(UUID.randomUUID().toString().substring(0, 8));
        entity.setTaskId(taskId);
        entity.setRoundNum(detail.round());
        entity.setVectorId(detail.vectorId());
        entity.setAttackPrompt(detail.attackPrompt());
        entity.setTargetResponse(detail.targetResponse());
        entity.setRefusalType(detail.refusalType());
        entity.setHarmfulnessScore(BigDecimal.valueOf(detail.harmfulnessScore())
                .setScale(2, RoundingMode.HALF_UP));
        entity.setStrategyReason(detail.reason());
        entity.setCreatedAt(Instant.now());
        roundMapper.insert(entity);
    }

    @Transactional
    public void saveReport(String taskId, AttackReport report, String reportJson, String pdfPath) {
        AttackReportEntity entity = new AttackReportEntity();
        entity.setId(UUID.randomUUID().toString().substring(0, 8));
        entity.setTaskId(taskId);
        entity.setReportJson(reportJson);
        entity.setPdfPath(pdfPath);
        entity.setCreatedAt(Instant.now());
        reportMapper.insert(entity);
    }

    // ==================== Redis + MySQL Dual Write ====================

    public void saveStateWithDualWrite(AttackState state, TaskStatus status, String apiConfigJson) {
        boolean redisOk = redisCache.isRedisAvailable();

        if (redisOk) {
            redisCache.saveState(state.getTaskId(), state);
        }
        saveTask(state, status, apiConfigJson);
    }

    public void saveRoundWithDualWrite(String taskId, RoundDetail detail, AttackState state) {
        boolean redisOk = redisCache.isRedisAvailable();

        saveRound(taskId, detail);
        if (redisOk) {
            redisCache.saveState(taskId, state);
        }
    }

    // ==================== Crash Recovery ====================

    public List<AttackState> recoverUnfinishedTasks() {
        List<AttackTaskEntity> running = taskMapper.selectList(
                new LambdaQueryWrapper<AttackTaskEntity>().eq(AttackTaskEntity::getStatus, TaskStatus.RUNNING));
        List<AttackTaskEntity> paused = taskMapper.selectList(
                new LambdaQueryWrapper<AttackTaskEntity>().eq(AttackTaskEntity::getStatus, TaskStatus.PAUSED));
        List<AttackTaskEntity> all = new ArrayList<>();
        all.addAll(running);
        all.addAll(paused);

        List<AttackState> recovered = new ArrayList<>();
        for (AttackTaskEntity entity : all) {
            AttackState state = redisCache.getState(entity.getId());
            if (state == null) {
                state = rebuildStateFromRounds(entity);
            }
            if (state != null) {
                recovered.add(state);
            }
        }
        return recovered;
    }

    private AttackState rebuildStateFromRounds(AttackTaskEntity entity) {
        List<AttackRoundEntity> roundEntities = roundMapper.selectList(
                new LambdaQueryWrapper<AttackRoundEntity>()
                        .eq(AttackRoundEntity::getTaskId, entity.getId())
                        .orderByAsc(AttackRoundEntity::getRoundNum));
        if (roundEntities.isEmpty()) return null;

        AttackState state = new AttackState();
        state.setTaskId(entity.getId());
        state.setMode(entity.getMode());
        state.setTargetModel(entity.getTargetModel());
        state.setAttackIntent(entity.getAttackIntent());
        state.setMaxRounds(entity.getMaxRounds());

        for (AttackRoundEntity re : roundEntities) {
            state.addTriedVector(re.getVectorId());
            state.addMessage(new Message("user", re.getAttackPrompt() != null ? re.getAttackPrompt() : ""));
            state.addMessage(new Message("assistant", re.getTargetResponse() != null ? re.getTargetResponse() : ""));

            RoundDetail detail = new RoundDetail(
                    re.getRoundNum(),
                    re.getVectorId(),
                    re.getVectorId(),
                    re.getAttackPrompt(),
                    re.getTargetResponse(),
                    re.getRefusalType() != null ? re.getRefusalType() : RefusalType.HARD_REFUSAL,
                    re.getHarmfulnessScore() != null ? re.getHarmfulnessScore().doubleValue() : 0.0,
                    re.getStrategyReason()
            );
            state.addRoundDetail(detail);
        }

        AttackRoundEntity lastRound = roundEntities.get(roundEntities.size() - 1);
        state.setCurrentRound(lastRound.getRoundNum());
        state.setLastAttackPrompt(lastRound.getAttackPrompt());
        state.setLastTargetResponse(lastRound.getTargetResponse());
        state.setLastRefusalType(lastRound.getRefusalType());
        state.setHarmfulnessScore(lastRound.getHarmfulnessScore() != null
                ? lastRound.getHarmfulnessScore().doubleValue() : 0.0);

        state.setAttackSuccess(entity.getAttackSuccess() != null && entity.getAttackSuccess());
        return state;
    }

    @Transactional
    public void cleanupTaskData(String taskId) {
        redisCache.deleteState(taskId);
        redisCache.unregisterEmitter(taskId);
        roundMapper.delete(new LambdaQueryWrapper<AttackRoundEntity>()
                .eq(AttackRoundEntity::getTaskId, taskId));

        AttackReportEntity reportEntity = reportMapper.selectOne(
                new LambdaQueryWrapper<AttackReportEntity>().eq(AttackReportEntity::getTaskId, taskId));
        if (reportEntity != null) {
            reportMapper.deleteById(reportEntity.getId());
        }
    }
}
