package com.jailbreak.agent.session;

import com.jailbreak.agent.model.AttackState;
import com.jailbreak.agent.model.CreateSessionRequest;
import com.jailbreak.agent.model.RoundDetail;

import java.util.List;

/**
 * 会话管理 —— 交互模式专用接口。
 * <p>
 * 生命周期: 创建 → 进行中(多轮循环) → 完成/超限 → 归档。
 * <p>
 * 交互模式下，编排引擎在 execute_attack 节点挂起等待用户手动粘贴，
 * 因此每次用户提交都触发一轮独立的编排引擎推进。
 */
public interface SessionService {

    /**
     * 创建交互会话并初始化 AttackState。
     *
     * @param request 含 targetModel, attackIntent, maxRounds
     * @return 生成的 sessionId (UUID)
     */
    String createSession(CreateSessionRequest request);

    /**
     * 用户提交目标回答，触发编排引擎推进一个节点（评估→路由→决策）。
     *
     * @param sessionId      会话 ID
     * @param targetResponse 用户粘贴的目标模型回答
     * @return 更新后的 AttackState（含新生成的 Prompt，供前端展示）
     */
    AttackState submitAnswer(String sessionId, String targetResponse);

    /**
     * 获取会话的完整对话历史（所有已完成轮次）。
     *
     * @param sessionId 会话 ID
     * @return 所有已完成轮次的详情列表
     */
    List<RoundDetail> getHistory(String sessionId);

    /**
     * 获取当前会话的攻击状态。
     *
     * @param sessionId 会话 ID
     * @return 当前攻击状态，不存在时返回 null
     */
    AttackState getState(String sessionId);

    /**
     * 终止会话。
     *
     * @param sessionId 会话 ID
     */
    void terminate(String sessionId);
}
