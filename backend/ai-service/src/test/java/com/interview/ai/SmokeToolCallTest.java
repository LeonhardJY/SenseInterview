package com.interview.ai;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.junit.jupiter.api.Test;

/**
 * 冒烟测试：验证 langchain4j 0.33 + DeepSeek 的 function calling 是否可用。
 * <p>
 * 若此测试失败（DeepSeek 不兼容 OpenAI 工具格式），则需切换星火 API。
 */
public class SmokeToolCallTest {

    /** 模拟面试官可用的工具 */
    public static class InterviewTools {

        @Tool("查询题库中与关键词相关的面试题")
        public String searchQuestions(String keyword) {
            return "【" + keyword + " 高频题】HashMap 底层是数组+链表+红黑树，容量为2的幂次，" +
                    "扩容因子0.75，加载到阈值触发扩容；ConcurrentHashMap 用 CAS + synchronized 保证线程安全。";
        }

        @Tool("获取当前面试环境信息")
        public String getEnvInfo() {
            return "当前是 Java 后端模拟面试，难度：中级，时长：40 分钟";
        }
    }

    @Test
    void deepseekToolCallWorks() {
        String apiKey = System.getenv("SENSE_LLM_KEY");
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("sk-placeholder")) {
            throw new IllegalStateException("未设置 SENSE_LLM_KEY 环境变量");
        }

        ChatLanguageModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com")
                .apiKey(apiKey)
                .modelName(System.getenv().getOrDefault("SENSE_LLM_MODEL", "deepseek-v4-flash"))
                .build();

        interface Assistant {
            String ask(String question);
        }

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(model)
                .tools(new InterviewTools())
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();

        String answer = assistant.ask("面试开始了，先调用环境工具看看面试设置，再调用题库工具查一个 HashMap 相关的题给我出题。");
        System.out.println("==== 冒烟测试结果 ====");
        System.out.println(answer);
    }
}
