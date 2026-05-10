package com.jailbreak.agent.persistence.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jailbreak.agent.persistence.entity.AttackRoundEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttackRoundJpaRepository extends BaseMapper<AttackRoundEntity> {
}
