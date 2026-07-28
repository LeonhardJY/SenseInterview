package com.interview.interview.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.interview.entity.InterviewTask;
import com.interview.interview.mapper.InterviewTaskMapper;
import com.interview.common.exception.BusinessException;
import com.interview.interview.service.InterviewSessionService;
import com.interview.interview.service.InterviewTaskService;
import com.interview.interview.websocket.InterviewWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InterviewTaskServiceImpl extends ServiceImpl<InterviewTaskMapper, InterviewTask> implements InterviewTaskService {

    private final InterviewSessionService sessionService;
    private final InterviewWebSocketHandler webSocketHandler;

    @Override
    public InterviewTask createTask(Long userId, String jobName, String mode, String difficulty) {
        InterviewTask task = new InterviewTask();
        task.setUserId(userId);
        task.setJobName(jobName);
        task.setMode(mode);
        task.setDifficulty(difficulty);
        task.setStatus("CREATED");
        task.setCreateTime(LocalDateTime.now());
        save(task);

        // 初始化 Redis 会话缓存
        sessionService.initSession(task.getId(), userId, jobName, mode, difficulty);

        return task;
    }

    @Override
    public InterviewTask startTask(Long taskId) {
        InterviewTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException("面试任务不存在");
        }
        task.setStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        updateById(task);

        // 更新会话状态
        sessionService.updateStatus(taskId, "RUNNING");

        // WebSocket 推送
        webSocketHandler.broadcastToRoom(String.valueOf(taskId), "STATUS_UPDATE",
                java.util.Map.of("status", "RUNNING", "startTime", LocalDateTime.now().toString()));

        return task;
    }

    @Override
    public InterviewTask endTask(Long taskId) {
        InterviewTask task = getById(taskId);
        if (task == null) {
            throw new BusinessException("面试任务不存在");
        }
        task.setStatus("FINISHED");
        task.setEndTime(LocalDateTime.now());
        updateById(task);

        // 更新会话状态
        sessionService.updateStatus(taskId, "FINISHED");

        // WebSocket 推送
        webSocketHandler.broadcastToRoom(String.valueOf(taskId), "STATUS_UPDATE",
                java.util.Map.of("status", "FINISHED", "endTime", LocalDateTime.now().toString()));

        return task;
    }
}