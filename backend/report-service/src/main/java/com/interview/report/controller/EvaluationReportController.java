package com.interview.report.controller;

import com.interview.common.result.Result;
import com.interview.report.entity.EvaluationReport;
import com.interview.report.service.EvaluationReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "报告管理", description = "报告相关接口")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class EvaluationReportController {

    private final EvaluationReportService evaluationReportService;

    @Operation(summary = "获取面试报告")
    @GetMapping("/{taskId}")
    public Result<EvaluationReport> getReportByTaskId(@PathVariable("taskId") Long taskId) {
        EvaluationReport report = evaluationReportService.findByTaskId(taskId);
        return Result.success(report);
    }

    @Operation(summary = "保存面试报告")
    @PostMapping("/save")
    public Result<Void> saveReport(@RequestBody EvaluationReport report) {
        evaluationReportService.save(report);
        return Result.success();
    }
}