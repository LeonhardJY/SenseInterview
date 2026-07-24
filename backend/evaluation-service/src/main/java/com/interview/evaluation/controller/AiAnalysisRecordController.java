package com.interview.evaluation.controller;

import com.interview.common.result.Result;
import com.interview.evaluation.entity.AiAnalysisRecord;
import com.interview.evaluation.service.AiAnalysisRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评测管理", description = "评测相关接口")
@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class AiAnalysisRecordController {

    private final AiAnalysisRecordService aiAnalysisRecordService;

    @Operation(summary = "获取面试分析记录")
    @GetMapping("/task/{taskId}")
    public Result<List<AiAnalysisRecord>> getAnalysisByTaskId(@PathVariable Long taskId) {
        List<AiAnalysisRecord> list = aiAnalysisRecordService.findByTaskId(taskId);
        return Result.success(list);
    }

    @Operation(summary = "保存分析记录")
    @PostMapping("/save")
    public Result<Void> saveAnalysis(@RequestBody AiAnalysisRecord record) {
        aiAnalysisRecordService.save(record);
        return Result.success();
    }
}