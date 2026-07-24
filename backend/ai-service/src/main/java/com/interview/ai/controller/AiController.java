package com.interview.ai.controller;

import com.interview.ai.service.AsrService;
import com.interview.ai.service.LlmService;
import com.interview.ai.service.NlpService;
import com.interview.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI服务", description = "AI相关接口")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AsrService asrService;
    private final NlpService nlpService;
    private final LlmService llmService;

    @Operation(summary = "语音转文本")
    @PostMapping("/speech-to-text")
    public Result<String> speechToText(@RequestParam("audioUrl") String audioUrl) {
        String text = asrService.speechToText(audioUrl);
        return Result.success(text);
    }

    @Operation(summary = "文本语义分析")
    @PostMapping("/analyze-text")
    public Result<Map<String, Object>> analyzeText(@RequestParam("text") String text) {
        Map<String, Object> result = nlpService.analyzeText(text);
        return Result.success(result);
    }

    @Operation(summary = "生成面试问题")
    @PostMapping("/generate-question")
    public Result<String> generateQuestion(@RequestParam("jobName") String jobName, @RequestParam("difficulty") String difficulty) {
        String question = llmService.generateQuestion(jobName, difficulty);
        return Result.success(question);
    }

    @Operation(summary = "生成追问")
    @PostMapping("/generate-follow-up")
    public Result<String> generateFollowUp(@RequestParam("question") String question, @RequestParam("answer") String answer) {
        String followUp = llmService.generateFollowUp(question, answer);
        return Result.success(followUp);
    }

    @Operation(summary = "生成面试评价")
    @PostMapping("/generate-evaluation")
    public Result<String> generateEvaluation(@RequestBody EvaluationRequest request) {
        String evaluation = llmService.generateEvaluation(request.getQuestions(), request.getAnswers());
        return Result.success(evaluation);
    }

    @lombok.Data
    public static class EvaluationRequest {
        private String[] questions;
        private String[] answers;
    }
}