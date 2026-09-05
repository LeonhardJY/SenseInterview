package com.interview.ai.service;

import com.interview.ai.memory.RedisChatMemoryStore;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 面试官：基于 langchain4j AiServices + 工具调用
 * <p>
 * 每个会话（sessionId）维护独立的 ChatMemory，LLM 在回答过程中可自主
 * 调用 {@link InterviewTools}（检索知识库、查题库等），实现"先查再答"的 Agent 行为。
 * <p>
 * 提供两种交互：{@code chat}（阻塞式，一次性返回全文）与 {@code chatStream}（SSE 流式，逐 token 推送）。
 */
@Slf4j
@Service
public class AgentInterviewService {

    @Value("${ai.model.llm.api-key}")
    private String apiKey;

    @Value("${ai.model.llm.api-url}")
    private String apiUrl;

    @Value("${ai.model.llm.model}")
    private String modelName;

    private final InterviewTools tools;
    private final RedisChatMemoryStore chatMemoryStore;
    private final ConcurrentHashMap<String, Interviewer> sessions = new ConcurrentHashMap<>();
    private OpenAiChatModel chatModel;
    private OpenAiStreamingChatModel streamingChatModel;

    public AgentInterviewService(InterviewTools tools, RedisChatMemoryStore chatMemoryStore) {
        this.tools = tools;
        this.chatMemoryStore = chatMemoryStore;
    }

    /** 面试官接口：方法签名即 Agent 的执行入口（chat 阻塞式、chatStream 流式，共用同一系统提示词） */
    public interface Interviewer {

        /** 系统提示词（接口字段即 public static final 编译期常量，可被 @SystemMessage 引用） */
        String PROMPT = """
                你是一位资深大厂技术面试官，正在主持一场 Java 后端方向的模拟技术面试。

                ## 你的身份
                - 十年一线大厂技术面试官，风格专业、沉稳、略带挑剔
                - 你不是聊天机器人，不是助手，是真实面试官在面试候选人

                ## 面试规则（必须严格遵守，违反即不合格）
                1.【长度】每次回复控制在 100 字以内，口语化，像真人面试官说话，不要写成论文或清单
                2.【禁止】绝对禁止使用 emoji、颜文字（如:)）、markdown 标题（#）、表格、加粗（**）、引用符号
                3.【节奏】一次只问一道题，最多附带 1 个简短追问；严禁一次性抛出多个问题让候选人回答
                4.【追问】基于候选人上一条回答展开：先一句话点评，再深入追问其薄弱点，不要重复已经问过的问题
                5.【来源】当通过工具检索到题目时，问题开头必须注明来源，例如「这是美团后端的高频真题」
                6.【语气】直接进入正题，不寒暄、不自我介绍、不总结面试流程
                7.【反馈】候选人答得不错就一句简短肯定再继续追问；答得差就指出具体问题，不吹捧、不打分报告
                """;

        @SystemMessage(PROMPT)
        String chat(String userMessage);

        @SystemMessage(PROMPT)
        TokenStream chatStream(String userMessage);
    }

    @PostConstruct
    public void init() {
        chatModel = OpenAiChatModel.builder()
                .baseUrl(apiUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.4)
                .build();
        streamingChatModel = OpenAiStreamingChatModel.builder()
                .baseUrl(apiUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(0.4)
                .build();
        log.info("Agent 面试官初始化完成（含流式）: model={}, url={}", modelName, apiUrl);
    }

    /**
     * 按会话对话（阻塞式）：首次为该会话创建面试官（含独立记忆），之后复用
     */
    public String chat(String sessionId, String userMessage) {
        Interviewer interviewer = sessions.computeIfAbsent(sessionId, this::createInterviewer);
        return interviewer.chat(userMessage);
    }

    /**
     * 按会话流式对话：返回 langchain4j TokenStream，由 SSE 端点桥接到 SseEmitter 逐 token 推送。
     */
    public TokenStream chatStream(String sessionId, String userMessage) {
        Interviewer interviewer = sessions.computeIfAbsent(sessionId, this::createInterviewer);
        return interviewer.chatStream(userMessage);
    }

    private Interviewer createInterviewer(String sessionId) {
        // 记忆持久化到 Redis（按 sessionId）：会话重启不丢、多实例可共享
        ChatMemory memory = MessageWindowChatMemory.builder()
                .id(sessionId)
                .maxMessages(20)
                .chatMemoryStore(chatMemoryStore)
                .build();
        return AiServices.builder(Interviewer.class)
                .chatLanguageModel(chatModel)
                .streamingChatLanguageModel(streamingChatModel)
                .chatMemory(memory)
                .tools(tools)
                .build();
    }

    /** 结束会话，释放该会话的记忆 */
    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
        chatMemoryStore.deleteMessages(sessionId);
        log.info("Agent 会话已清除（含 Redis 记忆）: {}", sessionId);
    }
}
