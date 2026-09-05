package com.interview.interview.service.impl;

import com.interview.interview.entity.InterviewRecord;
import com.interview.interview.service.InterviewAnswerService;
import com.interview.interview.service.InterviewFlowService;
import com.interview.interview.service.InterviewRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 面试提答交流服务实现。
 * <p>
 * 一轮问答的 DB 双写在一个事务内完成（@Transactional），避免"记录写了、回答没写"的脏数据；
 * Redis 会话缓存由调用方在事务提交后写，防止缓存与 DB 不一致。
 */
@Service
@RequiredArgsConstructor
public class InterviewFlowServiceImpl implements InterviewFlowService {

    private final InterviewRecordService interviewRecordService;
    private final InterviewAnswerService interviewAnswerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InterviewRecord recordAnswer(Long taskId, Integer roundNum, String question,
                                        String answerText, String audioUrl, String videoUrl) {
        InterviewRecord record = interviewRecordService.createRecord(taskId, roundNum, question);
        if (StringUtils.hasText(answerText)) {
            interviewAnswerService.createAnswer(record.getId(), answerText, audioUrl, videoUrl);
        }
        return record;
    }
}
