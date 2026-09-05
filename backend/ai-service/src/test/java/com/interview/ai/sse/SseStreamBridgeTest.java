package com.interview.ai.sse;

import dev.langchain4j.service.OnCompleteOrOnError;
import dev.langchain4j.service.OnError;
import dev.langchain4j.service.OnStart;
import dev.langchain4j.service.TokenStream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SSE 流式桥接单元测试。
 * <p>
 * langchain4j 0.33 的 TokenStream 是分阶段流式 API：
 * onNext → OnCompleteOrOnError → onComplete → OnError → onError → OnStart → start()。
 * 本测试逐阶段 mock，验证 token/完成/错误被正确转发到 SseEmitter，且最终 start() 被调用。
 */
class SseStreamBridgeTest {

    private final TokenStream tokenStream = mock(TokenStream.class);
    private final OnCompleteOrOnError afterNext = mock(OnCompleteOrOnError.class);
    private final OnError afterComplete = mock(OnError.class);
    private final OnStart afterError = mock(OnStart.class);
    private final SseEmitter emitter = mock(SseEmitter.class);

    private void stubChain() {
        when(tokenStream.onNext(any())).thenReturn(afterNext);
        when(afterNext.onComplete(any())).thenReturn(afterComplete);
        when(afterComplete.onError(any())).thenReturn(afterError);
    }

    @Test
    @SuppressWarnings("unchecked")
    void stream_逐token转发到emitter_完成时结束并启动流() throws IOException {
        stubChain();

        SseStreamBridge.stream(tokenStream, emitter);

        // onNext 回调：每个 token 都应 send 到 emitter
        ArgumentCaptor<Consumer> onNext = ArgumentCaptor.forClass(Consumer.class);
        verify(tokenStream).onNext(onNext.capture());
        onNext.getValue().accept("你");
        onNext.getValue().accept("好");
        verify(emitter).send("你");
        verify(emitter).send("好");

        // onComplete 回调：应 complete emitter
        ArgumentCaptor<Consumer> onComplete = ArgumentCaptor.forClass(Consumer.class);
        verify(afterNext).onComplete(onComplete.capture());
        onComplete.getValue().accept(null);
        verify(emitter).complete();

        // 桥接必须启动流
        verify(afterError).start();
    }

    @Test
    @SuppressWarnings("unchecked")
    void stream_出错时以异常结束emitter() {
        stubChain();

        SseStreamBridge.stream(tokenStream, emitter);

        ArgumentCaptor<Consumer> onError = ArgumentCaptor.forClass(Consumer.class);
        verify(afterComplete).onError(onError.capture());
        RuntimeException boom = new RuntimeException("boom");
        onError.getValue().accept(boom);
        verify(emitter).completeWithError(boom);
    }
}
