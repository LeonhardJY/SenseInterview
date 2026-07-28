package com.interview.interview.service;

import com.interview.interview.model.InterviewContext;

/**
 * 面试会话缓存服务
 * <p>
 * 用 Redis 缓存面试过程中的上下文状态，支持：
 * - 多轮对话历史保持
 * - 面试进度恢复（断线重连后继续）
 * - 跨服务共享上下文（AI服务、评测服务等）
 */
public interface InterviewSessionService {

    /**
     * 初始化面试会话
     */
    void initSession(Long taskId, Long userId, String jobName, String mode, String difficulty);

    /**
     * 获取面试上下文
     * @return 缓存中的上下文，不存在返回 null
     */
    InterviewContext getContext(Long taskId);

    /**
     * 更新面试上下文（全量覆盖）
     */
    void setContext(Long taskId, InterviewContext context);

    /**
     * 记录一轮问答
     */
    void addQaRecord(Long taskId, String question, String answer);

    /**
     * 记录一次情绪分析结果
     */
    void addEmotionRecord(Long taskId, String emotion, Double confidence);

    /**
     * 更新面试状态
     */
    void updateStatus(Long taskId, String status);

    /**
     * 获取当前轮次
     */
    Integer getCurrentRound(Long taskId);

    /**
     * 获取历史对话文本（给 LLM 用）
     */
    String getHistoryText(Long taskId);

    /**
     * 删除会话缓存（面试结束后清理）
     */
    void removeSession(Long taskId);

    /**
     * 检查会话是否存在
     */
    boolean hasSession(Long taskId);
}
