package com.interview.interview.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 面试会话上下文
 * <p>
 * 存储在一次面试过程中的所有动态状态，用 Redis 缓存。
 * 每次 LLM 调用时从这里取历史记录打包进 prompt，实现多轮对话连贯性。
 */
@Data
public class InterviewContext {

    /** 面试任务ID */
    private Long taskId;

    /** 用户ID */
    private Long userId;

    /** 岗位名称 */
    private String jobName;

    /** 面试模式：TEXT / VOICE / VIDEO */
    private String mode;

    /** 难度：初级 / 中级 / 高级 */
    private String difficulty;

    /** 当前轮次 */
    private Integer currentRound;

    /** 总轮数 */
    private Integer totalRounds;

    /** 面试状态：CREATED / RUNNING / FINISHED */
    private String status;

    /** 历史问答记录 */
    private List<QaPair> history;

    /** 用户简历摘要（从简历服务同步） */
    private String resumeSummary;

    /** 面部情绪分析记录（VIDEO 模式用） */
    private List<EmotionRecord> emotionHistory;

    /** 面试开始时间 */
    private LocalDateTime startTime;

    /** 面试结束时间 */
    private LocalDateTime endTime;

    /** 最近一次更新时间 */
    private LocalDateTime lastAccessTime;

    public InterviewContext() {
        this.currentRound = 0;
        this.totalRounds = 5;
        this.history = new ArrayList<>();
        this.emotionHistory = new ArrayList<>();
        this.status = "CREATED";
        this.lastAccessTime = LocalDateTime.now();
    }

    /**
     * 添加一条问答记录
     */
    public void addQa(String question, String answer) {
        if (this.history == null) {
            this.history = new ArrayList<>();
        }
        this.history.add(new QaPair(question, answer));
        this.currentRound = this.history.size();
        this.lastAccessTime = LocalDateTime.now();
    }

    /**
     * 添加一条情绪记录
     */
    public void addEmotion(String emotion, Double confidence) {
        if (this.emotionHistory == null) {
            this.emotionHistory = new ArrayList<>();
        }
        this.emotionHistory.add(new EmotionRecord(emotion, confidence));
        this.lastAccessTime = LocalDateTime.now();
    }

    /**
     * 构建 LLM 用的历史对话文本
     */
    public String buildHistoryText() {
        if (history == null || history.isEmpty()) {
            return "暂无问答记录";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            QaPair qa = history.get(i);
            sb.append("【问题").append(i + 1).append("】").append(qa.getQuestion()).append("\n");
            sb.append("【回答").append(i + 1).append("】").append(qa.getAnswer()).append("\n\n");
        }
        return sb.toString();
    }

    // ========== 内部类 ==========

    @Data
    public static class QaPair {
        private String question;
        private String answer;

        public QaPair() {}

        public QaPair(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    @Data
    public static class EmotionRecord {
        private String emotion;
        private Double confidence;
        private LocalDateTime timestamp;

        public EmotionRecord() {
            this.timestamp = LocalDateTime.now();
        }

        public EmotionRecord(String emotion, Double confidence) {
            this.emotion = emotion;
            this.confidence = confidence;
            this.timestamp = LocalDateTime.now();
        }
    }
}
