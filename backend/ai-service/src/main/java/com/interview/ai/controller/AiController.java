package com.interview.ai.controller;

import com.interview.ai.service.AsrService;
import com.interview.ai.service.FaceAnalysisService;
import com.interview.ai.service.LlmService;
import com.interview.ai.service.NlpService;
import com.interview.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@Tag(name = "AI服务", description = "AI相关接口")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AsrService asrService;
    private final NlpService nlpService;
    private final LlmService llmService;
    private final FaceAnalysisService faceAnalysisService;

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

    @Operation(summary = "生成追问（带历史上下文）")
    @PostMapping("/generate-follow-up-with-history")
    public Result<String> generateFollowUpWithHistory(@RequestBody FollowUpWithHistoryRequest request) {
        String followUp = llmService.generateFollowUpWithHistory(
                request.getHistory(),
                request.getCurrentQuestion(),
                request.getCurrentAnswer()
        );
        return Result.success(followUp);
    }

    @Operation(summary = "生成面试评价")
    @PostMapping("/generate-evaluation")
    public Result<String> generateEvaluation(@RequestBody EvaluationRequest request) {
        String evaluation = llmService.generateEvaluation(request.getQuestions(), request.getAnswers());
        return Result.success(evaluation);
    }

    // ========== 情绪分析 ==========

    @Operation(summary = "面部情绪分析（图片Base64）")
    @PostMapping("/analyze-emotion")
    public Result<FaceAnalysisService.EmotionResult> analyzeEmotion(@RequestBody EmotionRequest request) {
        FaceAnalysisService.EmotionResult result = faceAnalysisService.analyze(request.getImage());
        return Result.success(result);
    }

    // ========== 流式接口 (SSE) ==========

    @Operation(summary = "流式生成面试问题（SSE逐字推送）")
    @PostMapping(value = "/generate-question-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGenerateQuestion(@RequestBody Map<String, String> params) {
        String jobName = params.getOrDefault("jobName", "Java开发");
        String difficulty = params.getOrDefault("difficulty", "中级");
        SseEmitter emitter = new SseEmitter(180000L);
        llmService.streamGenerateQuestion(emitter, jobName, difficulty);
        return emitter;
    }

    @Operation(summary = "流式生成追问（带历史上下文，SSE逐字推送）")
    @PostMapping(value = "/generate-follow-up-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamGenerateFollowUp(@RequestBody FollowUpWithHistoryRequest request) {
        SseEmitter emitter = new SseEmitter(180000L);
        llmService.streamGenerateFollowUpWithHistory(
                emitter,
                request.getHistory(),
                request.getCurrentQuestion(),
                request.getCurrentAnswer()
        );
        return emitter;
    }

    @lombok.Data
    public static class EvaluationRequest {
        private String[] questions;
        private String[] answers;
    }

    @lombok.Data
    public static class FollowUpWithHistoryRequest {
        private List<Map<String, String>> history;
        private String currentQuestion;
        private String currentAnswer;
    }

    @lombok.Data
    public static class EmotionRequest {
        private String image;
    }
}