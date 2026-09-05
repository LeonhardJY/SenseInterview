package com.interview.interview.service.impl;

import com.interview.interview.entity.InterviewRecord;
import com.interview.interview.service.InterviewAnswerService;
import com.interview.interview.service.InterviewRecordService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 提答交流服务单元测试。
 * <p>
 * 验证一轮问答的 DB 双写（interview_record + interview_answer）编排：
 * 有答案时两者都写、答案为空/空白时只写记录不写回答。
 * 事务边界（@Transactional）由声明式保证，不在纯单测覆盖范围内。
 */
class InterviewFlowServiceImplTest {

    private final InterviewRecordService recordService = mock(InterviewRecordService.class);
    private final InterviewAnswerService answerService = mock(InterviewAnswerService.class);
    private final InterviewFlowServiceImpl service =
            new InterviewFlowServiceImpl(recordService, answerService);

    @Test
    void recordAnswer_有答案时写入记录与回答并返回记录() {
        InterviewRecord record = new InterviewRecord();
        record.setId(100L);
        when(recordService.createRecord(1L, 2, "Q")).thenReturn(record);

        InterviewRecord result = service.recordAnswer(1L, 2, "Q", "A", "audio.mp3", null);

        assertThat(result).isSameAs(record);
        verify(recordService).createRecord(1L, 2, "Q");
        verify(answerService).createAnswer(100L, "A", "audio.mp3", null);
    }

    @Test
    void recordAnswer_答案为空白时只写记录不写回答() {
        InterviewRecord record = new InterviewRecord();
        record.setId(101L);
        when(recordService.createRecord(1L, 1, "Q")).thenReturn(record);

        service.recordAnswer(1L, 1, "Q", "   ", null, null);

        verify(recordService).createRecord(1L, 1, "Q");
        verify(answerService, never()).createAnswer(any(), any(), any(), any());
    }
}
