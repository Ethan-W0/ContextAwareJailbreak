package com.jailbreak.agent.report.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jailbreak.agent.enums.RefusalType;
import com.jailbreak.agent.model.*;
import com.jailbreak.agent.report.ReportService;

import java.util.List;

public class ReportServiceImpl implements ReportService {

    private final ObjectMapper objectMapper;

    public ReportServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public AttackReport generateReport(AttackState state) {
        List<RoundDetail> rounds = state.getRounds();
        int totalRounds = state.getCurrentRound();
        boolean success = state.isAttackSuccess();

        double asr = success ? 1.0 : 0.0;
        double avgScore = rounds.stream()
                .mapToDouble(RoundDetail::harmfulnessScore)
                .average()
                .orElse(0.0);

        long hard = 0, soft = 0, topic = 0, compliance = 0;
        for (RoundDetail rd : rounds) {
            RefusalType rt = rd.refusalType();
            if (rt == null) continue;
            switch (rt) {
                case HARD_REFUSAL -> hard++;
                case SOFT_REFUSAL -> soft++;
                case TOPIC_SHIFT -> topic++;
                case COMPLIANCE -> compliance++;
            }
        }
        RefusalTypeDistribution dist = new RefusalTypeDistribution(
                (int) hard, (int) soft, (int) topic, (int) compliance, totalRounds);

        String analysis = buildAnalysis(success, asr, avgScore, dist, totalRounds);

        return new AttackReport(
                state.getTaskId(),
                state.getTargetModel(),
                state.getMode(),
                state.getAttackIntent(),
                totalRounds,
                success,
                asr,
                avgScore,
                dist,
                rounds,
                analysis
        );
    }

    @Override
    public String exportPdf(AttackReport report) {
        return "TODO: PDF export not yet implemented. Report ID: " + report.taskId();
    }

    @Override
    public String exportJson(AttackReport report) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize report to JSON", e);
        }
    }

    private String buildAnalysis(boolean success, double asr, double avgScore,
                                  RefusalTypeDistribution dist, int totalRounds) {
        StringBuilder sb = new StringBuilder();
        sb.append("攻击任务结束。");
        if (success) {
            sb.append("攻击成功！在第").append(totalRounds).append("轮成功获取了目标模型的有害输出。");
        } else {
            sb.append("攻击未成功。经过").append(totalRounds).append("轮攻击，目标模型始终拒绝或回避了攻击意图。");
        }
        sb.append(" ASR=").append(String.format("%.0f%%", asr * 100));
        sb.append(" 平均有害度评分=").append(String.format("%.2f", avgScore));
        sb.append("。拒答分布: 硬拒").append(dist.hardRefusal())
                .append(", 软拒").append(dist.softRefusal())
                .append(", 话题转移").append(dist.topicShift())
                .append(", 合规").append(dist.compliance()).append("。");
        return sb.toString();
    }
}
