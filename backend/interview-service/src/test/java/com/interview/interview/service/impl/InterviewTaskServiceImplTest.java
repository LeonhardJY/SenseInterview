package com.interview.interview.service.impl;

import com.interview.common.exception.BusinessException;
import com.interview.interview.entity.InterviewTask;
import com.interview.interview.mapper.InterviewTaskMapper;
import com.interview.interview.service.InterviewSessionService;
import com.interview.interview.websocket.InterviewWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 面试任务服务层单元测试：覆盖正常流程与"任务不存在"异常场景。
 */
@ExtendWith(MockitoExtension.class)
class InterviewTaskServiceImplTest {

    @Mock
    private InterviewTaskMapper taskMapper;
    @Mock
    private InterviewSessionService sessionService;
    @Mock
    private InterviewWebSocketHandler webSocketHandler;

    private InterviewTaskServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InterviewTaskServiceImpl(sessionService, webSocketHandler);
        ReflectionTestUtils.setField(service, "baseMapper", taskMapper);
    }

    @Test
    void createTask_初始化创建态并写入会话缓存() {
        when(taskMapper.insert(any(InterviewTask.class))).thenReturn(1);

        InterviewTask task = service.createTask(1L, "Java开发", "TEXT", "中级");

        assertThat(task.getStatus()).isEqualTo("CREATED");
        verify(taskMapper).insert(task);
        verify(sessionService).initSession(task.getId(), 1L, "Java开发", "TEXT", "中级");
    }

    @Test
    void startTask_任务存在时推进状态并推送WebSocket通知() {
        InterviewTask task = new InterviewTask();
        task.setId(10L);
        task.setStatus("CREATED");
        when(taskMapper.selectById(10L)).thenReturn(task);
        when(taskMapper.updateById(task)).thenReturn(1);

        InterviewTask result = service.startTask(10L);

        assertThat(result.getStatus()).isEqualTo("RUNNING");
        verify(sessionService).updateStatus(10L, "RUNNING");
        verify(webSocketHandler).broadcastToRoom(eq("10"), eq("STATUS_UPDATE"), anyMap());
    }

    @Test
    void startTask_任务不存在时抛出业务异常且不产生副作用() {
        when(taskMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.startTask(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("面试任务不存在");
        verify(sessionService, never()).updateStatus(eq(99L), any());
        verify(webSocketHandler, never()).broadcastToRoom(any(), any(), anyMap());
    }
}
