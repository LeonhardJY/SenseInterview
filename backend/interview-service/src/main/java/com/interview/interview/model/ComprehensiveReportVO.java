package com.interview.interview.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 综合面试报告 VO
 * <p>
 * 融合文本评分 + 情绪分析 + 面试记录的完整报告视图
 */
@Data
public class ComprehensiveReportVO {

    // ===== 基础信息 =====
    private Long taskId;
    private String jobName;
    private String mode;
    private String difficulty;
    private Integer totalRounds;
    private String duration;

    // ===== 评分 =====
    private BigDecimal totalScore;
    private Integer professionalScore;
    private Integer communicationScore;
    private Integer logicScore;
    private Integer emotionScore;
    private Integer confidenceScore;

    // ===== 文本评估 =====
    private String summary;
    private String suggestion;

    // ===== 情绪分析 =====
    private EmotionSummary emotionSummary;

    // ===== 问答记录 =====
    private List<QaRecord> records;

    @Data
    public static class EmotionSummary {
        private String dominantEmotion;   // 主要情绪
        private String emotionLabel;      // 中文标签
        private int totalFrames;          // 总分析帧数
        private int emotionChanges;       // 情绪变化次数
        private List<EmotionDistribution> distribution; // 情绪分布
    }

    @Data
    public static class EmotionDistribution {
        private String emotion;
        private String label;
        private int count;
        private double percentage;
    }

    @Data
    public static class QaRecord {
        private int round;
        private String question;
        private String answer;
    }
}
