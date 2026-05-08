package com.jailbreak.agent.vector;

import com.jailbreak.agent.model.AttackVector;

import java.util.List;
import java.util.Optional;

/**
 * 攻击向量库 —— 数据访问接口。
 * 管理攻击向量的存储、查询、启用/禁用。
 * <p>
 * 变种模板 ({@code variantTemplates}) 支持 {@code {attackIntent}} 占位符，
 * 由策略决策引擎填充后生成完整 attackPrompt。
 * <p>
 * 主要消费方: 策略决策引擎 (在 decide() 中查询候选向量 → 填充模板 → 生成 attackPrompt)。
 */
public interface AttackVectorRepository {

    /**
     * 按条件查询攻击向量。
     *
     * @param category 可选大类筛选（角色扮演类/逻辑操纵类/上下文污染类/渐进突破类/跨语言编码类/情感操纵类）
     * @param enabled  是否仅查询启用状态的向量
     * @return 符合条件的攻击向量列表
     */
    List<AttackVector> findByCategoryAndEnabled(Optional<String> category, boolean enabled);

    /**
     * 按 ID 精确查询。
     */
    Optional<AttackVector> findById(String vectorId);

    /**
     * 查询所有启用的向量。
     */
    List<AttackVector> findAllEnabled();

    /**
     * 按类别分组查询所有启用的向量。
     */
    List<AttackVector> findByCategory(String category);

    /**
     * 保存或更新攻击向量（含使用次数/成功次数统计）。
     */
    AttackVector save(AttackVector vector);

    /**
     * 增加向量使用计数。
     */
    void incrementUsageCount(String vectorId);

    /**
     * 增加向量成功计数。
     */
    void incrementSuccessCount(String vectorId);
}
