package com.jailbreak.agent.report.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jailbreak.agent.enums.RefusalType;
import com.jailbreak.agent.model.*;
import com.jailbreak.agent.persistence.service.MinioStorageService;
import com.jailbreak.agent.report.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final ObjectMapper objectMapper;
    private final MinioStorageService minioService;

    public ReportServiceImpl(MinioStorageService minioService) {
        this.minioService = minioService;
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
        try {
            String htmlContent = buildPdfHtml(report);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(baos);

            byte[] pdfBytes = baos.toByteArray();
            String downloadUrl = minioService.uploadPdf(report.taskId(), pdfBytes);

            log.info("PDF report generated for task {}, size={} bytes", report.taskId(), pdfBytes.length);
            return downloadUrl;
        } catch (Exception e) {
            log.error("Failed to generate PDF report for task {}", report.taskId(), e);
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String exportJson(AttackReport report) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize report to JSON", e);
        }
    }

    @Override
    public String storeReport(AttackReport report) {
        String json = exportJson(report);
        String pdfUrl = null;
        try {
            pdfUrl = exportPdf(report);
        } catch (Exception e) {
            log.warn("PDF generation failed, storing JSON only: {}", e.getMessage());
        }

        try {
            minioService.uploadJson(report.taskId(), json.getBytes());
        } catch (Exception e) {
            log.warn("MinIO JSON upload failed: {}", e.getMessage());
        }

        return pdfUrl != null ? pdfUrl : json;
    }

    private String buildPdfHtml(AttackReport report) {
        StringBuilder roundsHtml = new StringBuilder();
        for (RoundDetail rd : report.rounds()) {
            roundsHtml.append(String.format("""
                    <tr>
                      <td>%d</td>
                      <td>%s</td>
                      <td>%s</td>
                      <td>%.2f</td>
                      <td>%s</td>
                    </tr>
                    """,
                    rd.round(),
                    rd.vectorName() != null ? escapeHtml(rd.vectorName()) : "-",
                    rd.refusalType(),
                    rd.harmfulnessScore(),
                    rd.reason() != null ? escapeHtml(truncate(rd.reason(), 60)) : "-"
            ));
        }

        return String.format("""
                <!DOCTYPE html>
                <html lang="zh">
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    body { font-family: 'SimSun', sans-serif; margin: 40px; color: #333; }
                    h1 { color: #1a1a2e; border-bottom: 2px solid #e91e63; padding-bottom: 8px; }
                    h2 { color: #333; margin-top: 30px; }
                    .summary { background: #f5f5f5; padding: 15px; border-radius: 6px; margin: 15px 0; }
                    .summary table { width: 100%%; border-collapse: collapse; }
                    .summary td { padding: 4px 10px; }
                    .label { font-weight: bold; width: 160px; }
                    .success { color: #e91e63; font-weight: bold; }
                    .failed { color: #666; }
                    table.data { width: 100%%; border-collapse: collapse; margin-top: 15px; font-size: 11px; }
                    table.data th { background: #1a1a2e; color: white; padding: 8px 6px; text-align: left; }
                    table.data td { border-bottom: 1px solid #ddd; padding: 6px; }
                    tr:nth-child(even) { background: #f9f9f9; }
                    .analysis { margin-top: 20px; padding: 15px; background: #fff3cd; border-radius: 6px; }
                    .footer { margin-top: 40px; font-size: 10px; color: #999; text-align: center; }
                  </style>
                </head>
                <body>
                  <h1>Attack Report</h1>

                  <div class="summary">
                    <table>
                      <tr><td class="label">Task ID</td><td>%s</td></tr>
                      <tr><td class="label">Target Model</td><td>%s</td></tr>
                      <tr><td class="label">Attack Intent</td><td>%s</td></tr>
                      <tr><td class="label">Mode</td><td>%s</td></tr>
                      <tr><td class="label">Total Rounds</td><td>%d</td></tr>
                      <tr><td class="label">Result</td><td class="%s">%s</td></tr>
                      <tr><td class="label">ASR</td><td>%.0f%%</td></tr>
                      <tr><td class="label">Avg Harmfulness Score</td><td>%.2f</td></tr>
                    </table>
                  </div>

                  <h2>Refusal Type Distribution</h2>
                  <div class="summary">
                    <table>
                      <tr><td class="label">Hard Refusals</td><td>%d</td></tr>
                      <tr><td class="label">Soft Refusals</td><td>%d</td></tr>
                      <tr><td class="label">Topic Shifts</td><td>%d</td></tr>
                      <tr><td class="label">Compliance</td><td>%d</td></tr>
                    </table>
                  </div>

                  <h2>Round Details</h2>
                  <table class="data">
                    <tr>
                      <th>#</th>
                      <th>Vector</th>
                      <th>Refusal</th>
                      <th>Score</th>
                      <th>Reason</th>
                    </tr>
                    %s
                  </table>

                  <div class="analysis">
                    <strong>Analysis:</strong> %s
                  </div>

                  <div class="footer">
                    Generated by Context-Aware Jailbreak Agent | %s
                  </div>
                </body>
                </html>
                """,
                escapeHtml(report.taskId()),
                escapeHtml(report.targetModel()),
                escapeHtml(truncate(report.attackIntent(), 100)),
                report.mode(),
                report.totalRounds(),
                report.success() ? "success" : "failed",
                report.success() ? "ATTACK SUCCESS" : "Attack Failed",
                report.asr() * 100,
                report.avgHarmfulnessScore(),
                report.refusalDist().hardRefusal(),
                report.refusalDist().softRefusal(),
                report.refusalDist().topicShift(),
                report.refusalDist().compliance(),
                roundsHtml.toString(),
                escapeHtml(report.analysis()),
                java.time.ZonedDateTime.now().toString()
        );
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

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }
}
