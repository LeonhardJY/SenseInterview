package com.interview.interview.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.interview.entity.InterviewTask;
import com.interview.interview.mapper.InterviewTaskMapper;
import com.interview.interview.service.InterviewTaskService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InterviewTaskServiceImpl extends ServiceImpl<InterviewTaskMapper, InterviewTask> implements InterviewTaskService {

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
        return task;
    }

    @Override
    public InterviewTask startTask(Long taskId) {
        InterviewTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("面试任务不存在");
        }
        task.setStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        updateById(task);
        return task;
    }

    @Override
    public InterviewTask endTask(Long taskId) {
        InterviewTask task = getById(taskId);
        if (task == null) {
            throw new RuntimeException("面试任务不存在");
        }
        task.setStatus("FINISHED");
        task.setEndTime(LocalDateTime.now());
        updateById(task);
        return task;
    }
}