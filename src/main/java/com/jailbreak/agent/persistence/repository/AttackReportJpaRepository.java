package com.jailbreak.agent.persistence.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jailbreak.agent.persistence.entity.AttackReportEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttackReportJpaRepository extends BaseMapper<AttackReportEntity> {
}
