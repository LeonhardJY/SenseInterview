package com.interview.interview.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 面试会话上下文
 * <p>
 * 存储在一次面试过程中的动态状态，用 Redis 缓存。
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

    /** 面试状态：CREATED / RUNNING / FINISHED */
    private String status;

    /** 历史问答记录 */
    private List<QaPair> history;

    /** 面试开始时间 */
    private LocalDateTime startTime;

    /** 面试结束时间 */
    private LocalDateTime endTime;

    /** 最近一次更新时间 */
    private LocalDateTime lastAccessTime;

    public InterviewContext() {
        this.currentRound = 0;
        this.history = new ArrayList<>();
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
}
