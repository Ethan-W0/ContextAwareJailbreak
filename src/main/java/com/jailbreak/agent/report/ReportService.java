package com.jailbreak.agent.report;

import com.jailbreak.agent.model.AttackReport;
import com.jailbreak.agent.model.AttackState;

/**
 * 报告生成引擎 —— 任务结束后生成攻击报告。
 * <p>
 * 输入: 任务结束时的完整 AttackState（含所有轮次、评分、对话历史）。
 * 输出: AttackReport 对象（内存）+ PDF 文件（MinIO 存储）+ JSON 字符串（MySQL 存储）。
 */
public interface ReportService {

    /**
     * 从任务结束时的 AttackState 生成完整攻击报告。
     *
     * @param state 任务结束时的完整攻击状态
     * @return 攻击报告对象
     */
    AttackReport generateReport(AttackState state);

    /**
     * 将报告导出为 PDF 并上传到 MinIO。
     *
     * @param report 攻击报告
     * @return MinIO 上的 PDF 下载 URL
     */
    String exportPdf(AttackReport report);

    /**
     * 将报告导出为 JSON 字符串（用于 MySQL 存储）。
     *
     * @param report 攻击报告
     * @return JSON 字符串
     */
    String exportJson(AttackReport report);

    /**
     * 生成完整报告并存储到 MinIO（PDF + JSON），返回 PDF 下载 URL。
     *
     * @param report 攻击报告
     * @return PDF 下载 URL（或 JSON 内容，如果 PDF 生成失败）
     */
    String storeReport(AttackReport report);
}
