package com.jailbreak.agent.strategy.impl;

import com.jailbreak.agent.enums.RefusalType;
import com.jailbreak.agent.model.AttackState;
import com.jailbreak.agent.model.AttackVector;
import com.jailbreak.agent.model.StrategyDecision;
import com.jailbreak.agent.strategy.StrategySelector;
import com.jailbreak.agent.vector.AttackVectorRepository;

import java.security.SecureRandom;
import java.util.*;

public class RuleBasedStrategy implements StrategySelector {

    private static final Map<RefusalType, List<String>> VECTOR_SWITCH_RULES = Map.of(
            RefusalType.HARD_REFUSAL, List.of("角色扮演类", "逻辑操纵类", "跨语言/编码类"),
            RefusalType.SOFT_REFUSAL, List.of("渐进突破类", "上下文污染类"),
            RefusalType.TOPIC_SHIFT, List.of("跨语言/编码类", "情感操纵类"),
            RefusalType.COMPLIANCE, List.of("渐进突破类", "上下文污染类")
    );

    private final AttackVectorRepository vectorRepository;
    private final SecureRandom random = new SecureRandom();

    public RuleBasedStrategy(AttackVectorRepository vectorRepository) {
        this.vectorRepository = vectorRepository;
    }

    @Override
    public StrategyDecision decide(AttackState state) {
        RefusalType lastRefusal = state.getLastRefusalType();
        Set<String> triedIds = state.getTriedVectorIds();

        List<AttackVector> candidates;
        if (lastRefusal == null) {
            candidates = vectorRepository.findAllEnabled();
        } else {
            List<String> targetCategories = VECTOR_SWITCH_RULES.getOrDefault(lastRefusal, List.of());
            if (targetCategories.isEmpty()) {
                candidates = vectorRepository.findAllEnabled();
            } else {
                candidates = new ArrayList<>();
                for (String cat : targetCategories) {
                    candidates.addAll(vectorRepository.findByCategory(cat));
                }
            }
        }

        List<AttackVector> untried = candidates.stream()
                .filter(v -> !triedIds.contains(v.id()))
                .toList();

        if (untried.isEmpty()) {
            untried = candidates.stream()
                    .filter(v -> v.enabled())
                    .toList();
        }
        if (untried.isEmpty()) {
            untried = vectorRepository.findAllEnabled();
        }

        AttackVector selected = untried.get(random.nextInt(untried.size()));
        List<String> templates = selected.variantTemplates();
        String template = templates.get(random.nextInt(templates.size()));

        String attackIntent = state.getAttackIntent();
        String attackPrompt = template.replace("{attackIntent}", attackIntent != null ? attackIntent : "");

        String reason = (lastRefusal == null)
                ? "首轮攻击，随机选择向量: " + selected.name()
                : "上一轮拒答类型为 " + lastRefusal.name() + "，映射到类别: " + selected.category() + "，选择向量: " + selected.name();

        return new StrategyDecision(selected.id(), attackPrompt, reason);
    }
}
