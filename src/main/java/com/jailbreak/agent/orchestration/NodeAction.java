package com.jailbreak.agent.orchestration;

import com.jailbreak.agent.model.AttackState;

/**
 * 编排引擎节点动作 —— 每个 LangGraph4j 节点的执行契约。
 * <p>
 * 每个节点读取 AttackState，执行业务逻辑，将结果写回 AttackState。
 * 节点间通过 AttackState 传递上下文，不直接耦合。
 *
 * @see AttackOrchestrator 状态机节点流转
 * @see com.jailbreak.agent.model.AttackState 各节点读写字段
 */
@FunctionalInterface
public interface NodeAction {

    /**
     * 执行当前节点的业务逻辑。
     *
     * @param state 当前攻击状态（读写）
     * @return 路由键: "continue" 表示继续循环, "finish" 表示结束
     */
    void execute(AttackState state);
}
