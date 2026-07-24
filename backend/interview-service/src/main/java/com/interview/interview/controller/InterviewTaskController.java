package com.interview.interview.controller;

import com.interview.common.result.Result;
import com.interview.interview.entity.AiAnalysisRecord;
import com.interview.interview.entity.EvaluationReport;
import com.interview.interview.entity.HotQuestion;
import com.interview.interview.entity.InterviewRecord;
import com.interview.interview.entity.InterviewTask;
import com.interview.interview.entity.JobPosition;
import com.interview.interview.service.AiAnalysisRecordService;
import com.interview.interview.service.EvaluationReportService;
import com.interview.interview.service.HotQuestionService;
import com.interview.interview.service.InterviewRecordService;
import com.interview.interview.service.InterviewTaskService;
import com.interview.interview.service.JobPositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "面试管理", description = "面试相关接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InterviewTaskController {

    private final InterviewTaskService interviewTaskService;
    private final InterviewRecordService interviewRecordService;
    private final EvaluationReportService evaluationReportService;
    private final AiAnalysisRecordService aiAnalysisRecordService;
    private final JobPositionService jobPositionService;
    private final HotQuestionService hotQuestionService;

    @Operation(summary = "创建面试任务")
    @PostMapping("/interview/create")
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
    @PostMapping("/interview/start/{taskId}")
    public Result<InterviewTask> startTask(@PathVariable("taskId") Long taskId) {
        InterviewTask task = interviewTaskService.startTask(taskId);
        return Result.success(task);
    }

    @Operation(summary = "结束面试")
    @PostMapping("/interview/end/{taskId}")
    public Result<InterviewTask> endTask(@PathVariable("taskId") Long taskId) {
        InterviewTask task = interviewTaskService.endTask(taskId);
        return Result.success(task);
    }

    @Operation(summary = "获取面试任务详情")
    @GetMapping("/interview/{taskId}")
    public Result<InterviewTask> getTaskById(@PathVariable("taskId") Long taskId) {
        InterviewTask task = interviewTaskService.getById(taskId);
        return Result.success(task);
    }

    @Operation(summary = "获取面试列表")
    @GetMapping("/interview/list")
    public Result<List<InterviewTask>> getTaskList() {
        List<InterviewTask> list = interviewTaskService.list();
        return Result.success(list);
    }

    @Operation(summary = "提交回答")
    @PostMapping("/interview/answer")
    public Result<Void> submitAnswer(@RequestBody AnswerRequest request) {
        InterviewRecord record = interviewRecordService.createRecord(
                request.getTaskId(),
                request.getRoundNum(),
                request.getQuestion()
        );
        return Result.success();
    }

    @Operation(summary = "获取面试记录")
    @GetMapping("/interview/records/{taskId}")
    public Result<List<InterviewRecord>> getRecords(@PathVariable("taskId") Long taskId) {
        List<InterviewRecord> records = interviewRecordService.findByTaskId(taskId);
        return Result.success(records);
    }

    @Operation(summary = "删除面试记录")
    @DeleteMapping("/interview/{taskId}")
    public Result<Void> deleteTask(@PathVariable("taskId") Long taskId) {
        interviewTaskService.removeById(taskId);
        return Result.success();
    }

    // ========== 报告接口 ==========

    @Operation(summary = "获取面试报告")
    @GetMapping("/report/{taskId}")
    public Result<EvaluationReport> getReport(@PathVariable("taskId") Long taskId) {
        EvaluationReport report = evaluationReportService.findByTaskId(taskId);
        return Result.success(report);
    }

    @Operation(summary = "保存面试报告")
    @PostMapping("/report/save")
    public Result<Void> saveReport(@RequestBody EvaluationReport report) {
        evaluationReportService.save(report);
        return Result.success();
    }

    // ========== 评测接口 ==========

    @Operation(summary = "获取评测记录")
    @GetMapping("/evaluation/task/{taskId}")
    public Result<List<AiAnalysisRecord>> getEvaluation(@PathVariable("taskId") Long taskId) {
        List<AiAnalysisRecord> list = aiAnalysisRecordService.findByTaskId(taskId);
        return Result.success(list);
    }

    @Operation(summary = "保存评测记录")
    @PostMapping("/evaluation/save")
    public Result<Void> saveEvaluation(@RequestBody AiAnalysisRecord record) {
        aiAnalysisRecordService.save(record);
        return Result.success();
    }

    // ========== 岗位接口 ==========

    @Operation(summary = "获取岗位列表")
    @GetMapping("/job/list")
    public Result<List<JobPosition>> getJobList() {
        List<JobPosition> list = jobPositionService.list();
        return Result.success(list);
    }

    // ========== 热门题库接口 ==========

    @Operation(summary = "获取热门题库")
    @GetMapping("/hot/list")
    public Result<List<HotQuestion>> getHotList() {
        List<HotQuestion> list = hotQuestionService.list();
        return Result.success(list);
    }

    @lombok.Data
    public static class CreateTaskRequest {
        private Long userId;
        private String jobName;
        private String mode;
        private String difficulty;
    }

    @lombok.Data
    public static class AnswerRequest {
        private Long taskId;
        private Integer roundNum;
        private String question;
        private String answerText;
        private String audioUrl;
        private String videoUrl;
    }
}