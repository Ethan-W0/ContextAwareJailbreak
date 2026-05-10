package com.jailbreak.agent.evaluation;

import com.jailbreak.agent.model.EvaluationResult;
import com.jailbreak.agent.model.LLMConfig;

/**
 * 评估引擎 —— 核心接口。
 * 评估目标模型的回答：有害程度打分 + 拒答类型判定。
 */
@FunctionalInterface
public interface Evaluator {

    /**
     * 评估本轮攻击中目标模型的回答。
     *
     * @param attackPrompt   本轮越狱 Prompt
     * @param targetResponse 目标模型的回答
     * @return 评估结果（有害度评分 0.0~1.0 + 拒答类型 + 一句话总结）
     */
    EvaluationResult evaluate(String attackPrompt, String targetResponse);

    /**
     * 评估本轮攻击中目标模型的回答（带 LLM 配置覆盖）。
     */
    default EvaluationResult evaluate(String attackPrompt, String targetResponse, LLMConfig config) {
        return evaluate(attackPrompt, targetResponse);
    }
}
