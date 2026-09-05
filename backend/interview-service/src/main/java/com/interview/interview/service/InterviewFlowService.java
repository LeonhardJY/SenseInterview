package com.interview.interview.service;

import com.interview.interview.entity.InterviewRecord;

/**
 * 面试提答交流服务：把一轮问答的数据库双写编排在一个事务内。
 */
public interface InterviewFlowService {

    /**
     * 原子写入一轮问答的 DB 记录（interview_record + interview_answer）。
     * <p>
     * 两步写在同一事务内，避免"问题记录写了、回答没写"的脏数据。
     * Redis 会话缓存不属于本事务，应由调用方在事务提交后再写，防止"DB 回滚但缓存已写"的不一致。
     *
     * @return 已创建的面试问题记录（含自增 id）
     */
    InterviewRecord recordAnswer(Long taskId, Integer roundNum, String question,
                                 String answerText, String audioUrl, String videoUrl);
}
