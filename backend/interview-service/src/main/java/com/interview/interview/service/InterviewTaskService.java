package com.interview.interview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.interview.entity.InterviewTask;

public interface InterviewTaskService extends IService<InterviewTask> {

    InterviewTask createTask(Long userId, String jobName, String mode, String difficulty);

    InterviewTask startTask(Long taskId);

    InterviewTask endTask(Long taskId);
}