package com.interview.ai.memory;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis 版对话记忆存储单元测试。
 * <p>
 * 目的：把 Agent 的 ChatMemory 从单机内存改为 Redis 持久化，使会话重启不丢、多实例可共享。
 * 序列化用 langchain4j 官方 ChatMessageSerializer/Deserializer（真实实现，不 mock）。
 */
class RedisChatMemoryStoreTest {

    @SuppressWarnings("unchecked")
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> valueOps = mock(ValueOperations.class);
    private final RedisChatMemoryStore store = new RedisChatMemoryStore(redis);

    @Test
    void updateMessages_序列化后带TTL写入Redis() {
        when(redis.opsForValue()).thenReturn(valueOps);
        List<ChatMessage> msgs = List.of(UserMessage.from("你好"), AiMessage.from("请自我介绍"));

        store.updateMessages("s1", msgs);

        ArgumentCaptor<String> jsonCap = ArgumentCaptor.forClass(String.class);
        verify(valueOps).set(eq("ai:memory:s1"), jsonCap.capture(), any(Duration.class));
        assertThat(jsonCap.getValue()).contains("你好").contains("请自我介绍");
    }

    @Test
    void getMessages_命中时反序列化为等价消息列表() {
        String json = ChatMessageSerializer.messagesToJson(List.of(UserMessage.from("并发题")));
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("ai:memory:s1")).thenReturn(json);

        List<ChatMessage> msgs = store.getMessages("s1");

        assertThat(msgs).hasSize(1);
        assertThat(((UserMessage) msgs.get(0)).singleText()).isEqualTo("并发题");
    }

    @Test
    void getMessages_未命中时返回空列表而非null() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("ai:memory:missing")).thenReturn(null);

        assertThat(store.getMessages("missing")).isEmpty();
    }

    @Test
    void deleteMessages_删除对应key() {
        store.deleteMessages("s1");

        verify(redis).delete("ai:memory:s1");
    }
}
