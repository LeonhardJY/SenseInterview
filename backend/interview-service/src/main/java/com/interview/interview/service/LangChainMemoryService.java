package com.interview.interview.service;

import com.interview.interview.model.InterviewContext;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * LangChain4j 对话记忆管理服务
 * <p>
 * 改写项目要求接入 LangChain 状态管理。
 * 使用 LangChain4j 的 MessageWindowChatMemory 管理多轮对话上下文，
 * 配合 Redis 会话缓存实现持久化。
 * <p>
 * 与 InterviewSessionService 的关系：
 * - InterviewSessionService 负责面试全生命周期状态（轮次、模式、情绪等）
 * - LangChainMemoryService 只负责 LLM 对话的消息历史管理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LangChainMemoryService {

    /** 对话窗口大小（保留最近 N 轮问答） */
    private static final int WINDOW_SIZE = 10;

    private final InterviewSessionService sessionService;

    /**
     * 为指定面试创建 ChatMemory（从 Redis 恢复历史）
     */
    public ChatMemory createMemory(Long taskId, String systemPrompt) {
        MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
                .maxMessages(WINDOW_SIZE * 2 + 1) // system + N*(user+ai)
                .build();

        // 添加 system prompt
        memory.add(new SystemMessage(systemPrompt));

        // 从 Redis 恢复历史问答
        InterviewContext context = sessionService.getContext(taskId);
        if (context != null && context.getHistory() != null) {
            for (InterviewContext.QaPair qa : context.getHistory()) {
                memory.add(new UserMessage(qa.getQuestion()));
                memory.add(new AiMessage(qa.getAnswer()));
            }
        }

        return memory;
    }

    /**
     * 将 ChatMemory 的消息转换为 DeepSeek API 格式
     */
    public List<java.util.Map<String, String>> toDeepSeekMessages(List<ChatMessage> messages) {
        return messages.stream()
                .map(msg -> {
                    String role = switch (msg.type()) {
                        case SYSTEM -> "system";
                        case USER -> "user";
                        case AI -> "assistant";
                        default -> "user";
                    };
                    return java.util.Map.of("role", role, "content", msg.text());
                })
                .toList();
    }
}
