package com.interview.interview.controller;

import com.interview.common.result.Result;
import com.interview.interview.entity.InterviewTask;
import com.interview.interview.service.InterviewTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "面试管理", description = "面试相关接口")
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewTaskController {

    private final InterviewTaskService interviewTaskService;

    @Operation(summary = "创建面试任务")
    @PostMapping("/create")
    public Result<InterviewTask> createTask(@RequestBody CreateTaskRequest request) {
        InterviewTask task = interviewTaskService.createTask(
                request.getUserId(),
                request.getJobName(),
                request.getMode(),
                request.getDifficulty()
        );
        return Result.success(task);
    }

    @Operation(summary = "开始面试")
    @PostMapping("/start/{taskId}")
    public Result<InterviewTask> startTask(@PathVariable Long taskId) {
        InterviewTask task = interviewTaskService.startTask(taskId);
        return Result.success(task);
    }

    @Operation(summary = "结束面试")
    @PostMapping("/end/{taskId}")
    public Result<InterviewTask> endTask(@PathVariable Long taskId) {
        InterviewTask task = interviewTaskService.endTask(taskId);
        return Result.success(task);
    }

    @Operation(summary = "获取面试任务详情")
    @GetMapping("/{taskId}")
    public Result<InterviewTask> getTaskById(@PathVariable Long taskId) {
        InterviewTask task = interviewTaskService.getById(taskId);
        return Result.success(task);
    }

    @Operation(summary = "获取面试列表")
    @GetMapping("/list")
    public Result<List<InterviewTask>> getTaskList() {
        List<InterviewTask> list = interviewTaskService.list();
        return Result.success(list);
    }

    @lombok.Data
    public static class CreateTaskRequest {
        private Long userId;
        private String jobName;
        private String mode;
        private String difficulty;
    }
}