package com.interview.ai.sse;

import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * 把 langchain4j 的 TokenStream 逐 token 转发到 Spring MVC 的 SseEmitter，
 * 实现真正的 SSE 流式输出：首 token 即到即推，用户不必等整段生成完。
 */
@Slf4j
public final class SseStreamBridge {

    private SseStreamBridge() {
    }

    public static void stream(TokenStream tokenStream, SseEmitter emitter) {
        tokenStream
                .onNext(token -> send(emitter, token))
                .onComplete(response -> emitter.complete())
                .onError(error -> {
                    log.error("SSE 流式生成失败: {}", error.getMessage());
                    emitter.completeWithError(error);
                })
                .start();
    }

    private static void send(SseEmitter emitter, String token) {
        try {
            emitter.send(token);
        } catch (IOException | IllegalStateException e) {
            // 客户端断开或 emitter 已完成：记录并以异常结束，停止后续推送
            log.warn("SSE 发送中断（客户端可能已断开）: {}", e.getMessage());
            emitter.completeWithError(e);
        }
    }
}
