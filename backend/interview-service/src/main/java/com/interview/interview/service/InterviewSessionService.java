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
     * 更新面试状态
     */
    void updateStatus(Long taskId, String status);
}
