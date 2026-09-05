package com.interview.ai.controller;

import com.interview.ai.service.AgentInterviewService;
import com.interview.ai.service.FaceAnalysisService;
import com.interview.ai.service.RagService;
import com.interview.ai.sse.SseStreamBridge;
import com.interview.common.result.Result;
import dev.langchain4j.service.TokenStream;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "AI服务", description = "AI相关接口")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final FaceAnalysisService faceAnalysisService;
    private final RagService ragService;
    private final AgentInterviewService agentInterviewService;

    // ========== Agent 面试官 & RAG ==========

    @Operation(summary = "Agent 面试官对话（可自主调用工具检索知识库/题库）")
    @PostMapping("/agent-chat")
    public Result<String> agentChat(@RequestBody AgentChatRequest request) {
        String answer = agentInterviewService.chat(request.getSessionId(), request.getMessage());
        return Result.success(answer);
    }

    @Operation(summary = "Agent 面试官流式对话（SSE，逐 token 推送，降首 token 感知延迟）")
    @PostMapping(value = "/agent-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter agentStream(@RequestBody AgentChatRequest request) {
        // 超时略放宽，覆盖长回答的流式生成（与前端一致 120s）
        SseEmitter emitter = new SseEmitter(120_000L);
        try {
            TokenStream tokenStream = agentInterviewService.chatStream(request.getSessionId(), request.getMessage());
            SseStreamBridge.stream(tokenStream, emitter);
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @Operation(summary = "结束 Agent 面试会话（释放记忆）")
    @PostMapping("/agent-end")
    public Result<Void> agentEnd(@RequestBody AgentChatRequest request) {
        agentInterviewService.clearSession(request.getSessionId());
        return Result.success();
    }

    @Operation(summary = "重建 RAG 知识库索引（从 knowledge 目录向量化写入 Qdrant）")
    @PostMapping("/rag/rebuild")
    public Result<Integer> rebuildRagIndex() {
        int count = ragService.rebuildIndex();
        return Result.success(count);
    }

    @Operation(summary = "RAG 知识库检索测试")
    @PostMapping("/rag/retrieve")
    public Result<List<String>> ragRetrieve(@RequestBody RagRetrieveRequest request) {
        List<String> docs = ragService.retrieve(request.getQuery());
        return Result.success(docs);
    }

    // ========== 情绪分析 ==========

    @Operation(summary = "面部情绪分析（图片Base64）")
    @PostMapping("/analyze-emotion")
    public Result<FaceAnalysisService.EmotionResult> analyzeEmotion(@RequestBody EmotionRequest request) {
        FaceAnalysisService.EmotionResult result = faceAnalysisService.analyze(request.getImage());
        return Result.success(result);
    }

    @lombok.Data
    public static class AgentChatRequest {
        private String sessionId;
        private String message;
    }

    @lombok.Data
    public static class RagRetrieveRequest {
        private String query;
    }

    @lombok.Data
    public static class EmotionRequest {
        private String image;
    }
}
