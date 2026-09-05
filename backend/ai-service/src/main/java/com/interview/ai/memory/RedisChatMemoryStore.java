package com.interview.ai.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis 版对话记忆存储：把 langchain4j 的 ChatMemory 持久化到 Redis，
 * 使 Agent 会话重启不丢、多实例可共享（替代单机内存 Map）。
 * <p>
 * 序列化用 langchain4j 官方 ChatMessageSerializer/Deserializer（含消息类型信息，能还原 User/Ai/Tool 消息）。
 * Key: ai:memory:{sessionId}，TTL 2 小时（与面试会话缓存一致）。
 */
@Slf4j
@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    private static final String KEY_PREFIX = "ai:memory:";
    private static final Duration TTL = Duration.ofHours(2);

    private final StringRedisTemplate redis;

    public RedisChatMemoryStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = redis.opsForValue().get(KEY_PREFIX + memoryId);
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (Exception e) {
            log.warn("对话记忆反序列化失败，返回空历史 — memoryId: {}, err: {}", memoryId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = ChatMessageSerializer.messagesToJson(messages);
        redis.opsForValue().set(KEY_PREFIX + memoryId, json, TTL);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        redis.delete(KEY_PREFIX + memoryId);
    }
}
