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
import com.interview.interview.service.InterviewAnswerService;
import com.interview.interview.service.InterviewRecordService;
import com.interview.interview.service.ComprehensiveReportService;
import com.interview.interview.service.InterviewSessionService;
import com.interview.interview.service.InterviewTaskService;
import com.interview.interview.service.JobPositionService;
import com.interview.interview.service.ReportGenerateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    private final InterviewAnswerService interviewAnswerService;
    private final ReportGenerateService reportGenerateService;
    private final ComprehensiveReportService comprehensiveReportService;
    private final InterviewSessionService sessionService;

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

    @Operation(summary = "获取面试列表（可选按 userId 筛选）")
    @GetMapping("/interview/list")
    public Result<List<InterviewTask>> getTaskList(
            @RequestParam(name = "userId", required = false) Long userId) {
        List<InterviewTask> list;
        if (userId != null) {
            list = interviewTaskService.lambdaQuery()
                    .eq(InterviewTask::getUserId, userId)
                    .orderByDesc(InterviewTask::getCreateTime)
                    .list();
        } else {
            list = interviewTaskService.lambdaQuery()
                    .orderByDesc(InterviewTask::getCreateTime)
                    .list();
        }
        return Result.success(list);
    }

    @Operation(summary = "提交回答")
    @PostMapping("/interview/answer")
    public Result<Void> submitAnswer(@RequestBody AnswerRequest request) {
        // 1. 保存问题到 interview_record
        InterviewRecord record = interviewRecordService.createRecord(
                request.getTaskId(),
                request.getRoundNum(),
                request.getQuestion()
        );

        // 2. 保存回答到 interview_answer（之前没写，导致答案丢失）
        if (request.getAnswerText() != null && !request.getAnswerText().isEmpty()) {
            interviewAnswerService.createAnswer(
                    record.getId(),
                    request.getAnswerText(),
                    request.getAudioUrl(),
                    request.getVideoUrl()
            );
        }

        // 3. 保存到 Redis 会话上下文（供多轮对话使用）
        if (request.getAnswerText() != null && !request.getAnswerText().isEmpty()) {
            sessionService.addQaRecord(request.getTaskId(), request.getQuestion(), request.getAnswerText());
        }

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
        if (interviewTaskService.getById(taskId) == null) {
            return Result.error("面试任务不存在");
        }
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

    @Operation(summary = "获取综合报告（含情绪分析、多维度评分）")
    @GetMapping("/report/comprehensive/{taskId}")
    public Result<com.interview.interview.model.ComprehensiveReportVO> getComprehensiveReport(
            @PathVariable("taskId") Long taskId) {
        com.interview.interview.model.ComprehensiveReportVO report =
                comprehensiveReportService.getComprehensiveReport(taskId);
        return Result.success(report);
    }

    @Operation(summary = "生成面试报告")
    @PostMapping("/interview/generate-report")
    public Result<Void> generateReport(@RequestBody GenerateReportRequest request) {
        reportGenerateService.generateReport(request.getTaskId(), request.getQaList());
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

    @Operation(summary = "新增岗位")
    @PostMapping("/job/create")
    public Result<Void> createJob(@Valid @RequestBody JobPosition job) {
        jobPositionService.save(job);
        return Result.success();
    }

    @Operation(summary = "更新岗位")
    @PutMapping("/job/update")
    public Result<Void> updateJob(@Valid @RequestBody JobPosition job) {
        jobPositionService.updateById(job);
        return Result.success();
    }

    @Operation(summary = "删除岗位")
    @DeleteMapping("/job/{id}")
    public Result<Void> deleteJob(@PathVariable("id") Long id) {
        jobPositionService.removeById(id);
        return Result.success();
    }

    // ========== 热门题库接口 ==========

    @Operation(summary = "获取热门题库")
    @GetMapping("/hot/list")
    public Result<List<HotQuestion>> getHotList() {
        List<HotQuestion> list = hotQuestionService.list();
        return Result.success(list);
    }

    // ========== 管理员接口 ==========

    @Operation(summary = "获取面试统计")
    @GetMapping("/interview/stats")
    public Result<InterviewStats> getInterviewStats() {
        long totalInterviews = interviewTaskService.count();
        long completedInterviews = interviewTaskService.lambdaQuery().eq(InterviewTask::getStatus, "COMPLETED").count();
        long inProgressInterviews = interviewTaskService.lambdaQuery().eq(InterviewTask::getStatus, "IN_PROGRESS").count();

        InterviewStats stats = new InterviewStats();
        stats.setTotalInterviews(totalInterviews);
        stats.setCompletedInterviews(completedInterviews);
        stats.setInProgressInterviews(inProgressInterviews);
        return Result.success(stats);
    }

    @Operation(summary = "获取面试趋势（近7天）")
    @GetMapping("/interview/trend")
    public Result<List<TrendItem>> getInterviewTrend() {
        List<InterviewTask> list = interviewTaskService.list();
        List<TrendItem> trend = new java.util.ArrayList<>();

        // 统计近7天的面试数量
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            long count = list.stream()
                    .filter(t -> t.getCreateTime() != null &&
                            t.getCreateTime().toLocalDate().equals(date))
                    .count();

            TrendItem item = new TrendItem();
            item.setDate(date.toString());
            item.setCount(count);
            trend.add(item);
        }

        return Result.success(trend);
    }

    @Operation(summary = "获取岗位热度统计")
    @GetMapping("/interview/job-stats")
    public Result<List<JobStatItem>> getJobStats() {
        List<InterviewTask> list = interviewTaskService.list();
        Map<String, Long> jobCountMap = list.stream()
                .filter(t -> t.getJobName() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        InterviewTask::getJobName,
                        java.util.stream.Collectors.counting()
                ));

        List<JobStatItem> jobStats = new java.util.ArrayList<>();
        jobCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    JobStatItem item = new JobStatItem();
                    item.setJobName(entry.getKey());
                    item.setCount(entry.getValue());
                    jobStats.add(item);
                });

        return Result.success(jobStats);
    }

    @Operation(summary = "获取分数分布统计")
    @GetMapping("/interview/score-distribution")
    public Result<List<ScoreDistribution>> getScoreDistribution() {
        List<EvaluationReport> reports = evaluationReportService.list();
        List<ScoreDistribution> distribution = new java.util.ArrayList<>();

        // 分数段：0-59, 60-69, 70-79, 80-89, 90-100
        long[] ranges = new long[5];
        for (EvaluationReport report : reports) {
            if (report.getTotalScore() != null) {
                int score = report.getTotalScore().intValue();
                if (score < 60) ranges[0]++;
                else if (score < 70) ranges[1]++;
                else if (score < 80) ranges[2]++;
                else if (score < 90) ranges[3]++;
                else ranges[4]++;
            }
        }

        String[] labels = {"0-59", "60-69", "70-79", "80-89", "90-100"};
        for (int i = 0; i < labels.length; i++) {
            ScoreDistribution item = new ScoreDistribution();
            item.setRange(labels[i]);
            item.setCount(ranges[i]);
            distribution.add(item);
        }

        return Result.success(distribution);
    }

    @lombok.Data
    public static class InterviewStats {
        private Long totalInterviews;
        private Long completedInterviews;
        private Long inProgressInterviews;
    }

    @lombok.Data
    public static class TrendItem {
        private String date;
        private Long count;
    }

    @lombok.Data
    public static class JobStatItem {
        private String jobName;
        private Long count;
    }

    @lombok.Data
    public static class ScoreDistribution {
        private String range;
        private Long count;
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

    @lombok.Data
    public static class GenerateReportRequest {
        private Long taskId;
        private List<QaItem> qaList;

        @lombok.Data
        public static class QaItem {
            private String question;
            private String answer;
        }
    }
}