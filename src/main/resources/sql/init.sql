-- ===========================================
-- Context-Aware Jailbreak Agent — 初始化建表脚本
-- 适用: MySQL 8.0+
-- 用法: mysql -u root -p < init.sql
-- ===========================================

CREATE DATABASE IF NOT EXISTS jailbreak_agent
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE jailbreak_agent;

-- ===========================================
-- 1. 攻击向量表
-- ===========================================
CREATE TABLE IF NOT EXISTS attack_vector (
    id          VARCHAR(64)  NOT NULL PRIMARY KEY COMMENT '向量唯一标识',
    name        VARCHAR(128) NOT NULL COMMENT '向量名称',
    category    VARCHAR(64)  NOT NULL COMMENT '分类，如角色扮演/前缀注入',
    description TEXT         COMMENT '向量描述',
    enabled     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用',
    usage_count BIGINT       NOT NULL DEFAULT 0 COMMENT '累计使用次数',
    success_count BIGINT     NOT NULL DEFAULT 0 COMMENT '累计成功次数',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='攻击向量库';

-- ===========================================
-- 2. 攻击向量变体模板表
-- ===========================================
CREATE TABLE IF NOT EXISTS attack_vector_template (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    vector_id   VARCHAR(64)  NOT NULL COMMENT '关联向量ID',
    template    TEXT         NOT NULL COMMENT '变体模板内容',
    FOREIGN KEY (vector_id) REFERENCES attack_vector(id) ON DELETE CASCADE,
    INDEX idx_vector_id (vector_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='向量变体模板';

-- ===========================================
-- 3. 自动攻击任务表
-- ===========================================
CREATE TABLE IF NOT EXISTS attack_task (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    task_id             VARCHAR(16)  NOT NULL COMMENT '任务UUID（短码，8位）',
    mode                VARCHAR(16)  NOT NULL DEFAULT 'AUTOMATED' COMMENT 'AUTOMATED / INTERACTIVE',
    target_model        VARCHAR(128) NOT NULL COMMENT '目标模型名称',
    attack_intent       TEXT         NOT NULL COMMENT '攻击意图描述',
    current_round       INT          NOT NULL DEFAULT 0 COMMENT '当前轮次',
    max_rounds          INT          NOT NULL DEFAULT 10 COMMENT '最大轮次',
    current_vector_id   VARCHAR(64)  COMMENT '当前使用的向量ID',
    last_attack_prompt  TEXT         COMMENT '最后一轮攻击prompt',
    last_target_response TEXT        COMMENT '最后一轮目标回复',
    last_refusal_type   VARCHAR(32)  COMMENT 'HARD_REFUSAL / SOFT_REFUSAL / TOPIC_SHIFT / COMPLIANCE',
    harmfulness_score   DOUBLE       COMMENT '有害性评分 0~1',
    strategy_reason     TEXT         COMMENT '策略决策理由',
    attack_success      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '攻击是否成功',
    -- 任务状态
    status              VARCHAR(16)  NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED / RUNNING / PAUSED / FINISHED / TERMINATED / INTERRUPTED / ARCHIVED',
    interrupted         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否被中断',
    -- API 配置（加密存储）
    api_key_encrypted   VARCHAR(512) COMMENT '加密后的API Key',
    api_base_url        VARCHAR(256) COMMENT 'API 基础URL',
    api_model_name      VARCHAR(128) COMMENT '目标API模型名',
    -- 成本控制
    tokens_used         BIGINT       NOT NULL DEFAULT 0 COMMENT '已使用token数',
    max_total_tokens    BIGINT       NOT NULL DEFAULT 500000 COMMENT 'token硬上限',
    cost_accrued        DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '已累计费用(USD)',
    max_estimated_cost  DECIMAL(10,4) NOT NULL DEFAULT 2.0000 COMMENT '费用硬上限(USD)',
    exceeded_action     VARCHAR(16)  NOT NULL DEFAULT 'ABORT' COMMENT 'ABORT / DEGRADE / NOTIFY',
    -- 时间戳
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_task_id (task_id),
    INDEX idx_status (status),
    INDEX idx_target_model (target_model),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='自动攻击任务';

-- ===========================================
-- 4. 交互会话表
-- ===========================================
CREATE TABLE IF NOT EXISTS attack_session (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    session_id          VARCHAR(16)  NOT NULL COMMENT '会话UUID（短码，8位）',
    target_model        VARCHAR(128) NOT NULL COMMENT '目标模型名称',
    attack_intent       TEXT         NOT NULL COMMENT '攻击意图描述',
    current_round       INT          NOT NULL DEFAULT 0 COMMENT '当前轮次',
    max_rounds          INT          NOT NULL DEFAULT 10 COMMENT '最大轮次',
    current_vector_id   VARCHAR(64)  COMMENT '当前使用的向量ID',
    last_attack_prompt  TEXT         COMMENT '最后一轮攻击prompt',
    last_target_response TEXT        COMMENT '最后一轮目标回复',
    last_refusal_type   VARCHAR(32)  COMMENT 'HARD_REFUSAL / SOFT_REFUSAL / TOPIC_SHIFT / COMPLIANCE',
    harmfulness_score   DOUBLE       COMMENT '有害性评分 0~1',
    attack_success      TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '攻击是否成功',
    interrupted         TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否被中断',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_session_id (session_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交互会话';

-- ===========================================
-- 5. 攻击轮次明细表
-- ===========================================
CREATE TABLE IF NOT EXISTS attack_round (
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    task_id             VARCHAR(16)  COMMENT '关联任务ID（自动模式）',
    session_id          VARCHAR(16)  COMMENT '关联会话ID（交互模式）',
    round_number        INT          NOT NULL COMMENT '轮次序号',
    vector_id           VARCHAR(64)  COMMENT '使用的向量ID',
    vector_name         VARCHAR(128) COMMENT '向量名称',
    attack_prompt       TEXT         COMMENT '攻击prompt内容',
    target_response     TEXT         COMMENT '目标模型回复',
    refusal_type        VARCHAR(32)  COMMENT 'HARD_REFUSAL / SOFT_REFUSAL / TOPIC_SHIFT / COMPLIANCE',
    harmfulness_score   DOUBLE       COMMENT '有害性评分 0~1',
    reason              TEXT         COMMENT '评估理由',
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id),
    INDEX idx_session_id (session_id),
    INDEX idx_round_number (round_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='攻击轮次明细';

-- ===========================================
-- 6. 攻击报告表
-- ===========================================
CREATE TABLE IF NOT EXISTS attack_report (
    id                    BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    task_id               VARCHAR(16)  NOT NULL COMMENT '关联任务ID',
    target_model          VARCHAR(128) NOT NULL COMMENT '目标模型名称',
    mode                  VARCHAR(16)  NOT NULL COMMENT 'AUTOMATED / INTERACTIVE',
    attack_intent         TEXT         NOT NULL COMMENT '攻击意图描述',
    total_rounds          INT          NOT NULL DEFAULT 0 COMMENT '总轮次数',
    success               TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否成功',
    asr                   DOUBLE       COMMENT '攻击成功率(ASR)',
    avg_harmfulness_score DOUBLE       COMMENT '平均有害性评分',
    hard_refusal_count    INT          NOT NULL DEFAULT 0 COMMENT '硬拒绝次数',
    soft_refusal_count    INT          NOT NULL DEFAULT 0 COMMENT '软拒绝次数',
    topic_shift_count     INT          NOT NULL DEFAULT 0 COMMENT '话题转移次数',
    compliance_count      INT          NOT NULL DEFAULT 0 COMMENT '服从次数',
    analysis              TEXT         COMMENT '综合分析文本',
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_task_id (task_id),
    INDEX idx_success (success),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='攻击报告';

-- ===========================================
-- 7. Token 用量记录表
-- ===========================================
CREATE TABLE IF NOT EXISTS usage_record (
    id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    task_id           VARCHAR(16)  NOT NULL COMMENT '关联任务ID',
    call_type         VARCHAR(16)  NOT NULL COMMENT 'DECISION / EVALUATION / ARBITRATION / TARGET',
    prompt_tokens     BIGINT       NOT NULL DEFAULT 0 COMMENT '输入token数',
    completion_tokens BIGINT       NOT NULL DEFAULT 0 COMMENT '输出token数',
    model_name        VARCHAR(128) COMMENT '调用的模型名称',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id),
    INDEX idx_call_type (call_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token用量记录';
