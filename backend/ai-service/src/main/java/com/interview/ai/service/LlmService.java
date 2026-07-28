package com.interview.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class LlmService {

    @Value("${ai.model.llm.api-url}")
    private String apiUrl;

    @Value("${ai.model.llm.api-key}")
    private String apiKey;

    @Value("${ai.model.llm.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用 DeepSeek API (单轮 system + user)
     */
    private String callDeepSeek(String systemPrompt, String userMessage) {
        return callDeepSeekWithMessages(systemPrompt, List.of(
                Map.of("role", "user", "content", userMessage)
        ));
    }

    /**
     * 调用 DeepSeek API (支持多轮消息列表)
     */
    private String callDeepSeekWithMessages(String systemPrompt, List<Map<String, String>> history) {
        String url = apiUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<Object> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        if (history != null) {
            messages.addAll(history);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.7);
        body.put("max_tokens", 2000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "抱歉，AI暂时无法回答，请稍后重试。";
        } catch (Exception e) {
            log.error("调用DeepSeek API失败: {}", e.getMessage());
            return "抱歉，AI服务出现异常，请稍后重试。";
        }
    }

    /**
     * 生成面试问题
     */
    public String generateQuestion(String jobName, String difficulty) {
        String difficultyDesc = switch (difficulty) {
            case "初级" -> "基础概念、入门知识、简单实践";
            case "中级" -> "深入原理、项目经验、常见问题解决";
            case "高级" -> "架构设计、性能优化、复杂场景处理";
            default -> "综合能力";
        };

        String systemPrompt = String.format(
                "你是一位拥有10年经验的%s技术面试官，正在对候选人进行%s级别面试。\n\n" +
                "## 要求\n" +
                "- 问题要贴近实际工作场景，不要问八股文\n" +
                "- %s\n" +
                "- 问题要能区分候选人的真实水平\n" +
                "- 只返回问题内容，不要有其他多余文字\n\n" +
                "请生成一个有深度的面试问题。",
                jobName, difficulty, difficultyDesc
        );
        return callDeepSeek(systemPrompt, "请生成一个面试问题");
    }

    /**
     * 生成追问 (单轮，无历史)
     */
    public String generateFollowUp(String question, String answer) {
        String systemPrompt =
                "你是一位经验丰富的技术面试官，正在对候选人进行深度面试。\n\n" +
                "## 追问原则\n" +
                "- 如果候选人回答正确但不够深入，追问细节和原理\n" +
                "- 如果候选人回答模糊，追问具体实现和案例\n" +
                "- 如果候选人回答错误，给出提示让他思考\n" +
                "- 追问要自然，像真实面试对话\n" +
                "- 只返回追问内容，不要有其他文字";

        String userMessage = String.format(
                "【原问题】%s\n\n【候选人回答】%s\n\n请根据回答内容进行有针对性的追问。",
                question, answer
        );
        return callDeepSeek(systemPrompt, userMessage);
    }

    /**
     * 基于历史对话生成追问 (多轮上下文感知)
     *
     * @param history        历史问答列表，每项含 question 和 answer
     * @param currentQuestion 当前问题
     * @param currentAnswer   当前回答
     * @return AI 生成的追问
     */
    public String generateFollowUpWithHistory(List<Map<String, String>> history,
                                               String currentQuestion,
                                               String currentAnswer) {
        String systemPrompt =
                "你是一位经验丰富的技术面试官，正在对候选人进行深度面试。\n\n" +
                "## 面试背景\n" +
                "这场面试已经进行了几轮对话，你需要参考前面的问答历史，\n" +
                "确保追问与前文连贯，不要重复问过的问题，要在已有基础上深入挖掘。\n\n" +
                "## 追问原则\n" +
                "- 如果候选人回答正确但不够深入，追问细节和原理\n" +
                "- 如果候选人回答模糊，追问具体实现和案例\n" +
                "- 如果候选人回答错误，给出提示让他思考\n" +
                "- 追问要自然，像真实面试对话\n" +
                "- 避免重复已问过的问题\n" +
                "- 如果某个话题已经问透了，可以自然过渡到相关新话题\n" +
                "- 只返回追问内容，不要有其他文字";

        // 构建历史消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        if (history != null) {
            for (Map<String, String> qa : history) {
                messages.add(Map.of("role", "user", "content",
                        "【我的问题】" + qa.getOrDefault("question", "")));
                messages.add(Map.of("role", "assistant", "content",
                        "【候选人回答】" + qa.getOrDefault("answer", "")));
            }
        }
        // 当前这一轮
        messages.add(Map.of("role", "user", "content",
                "【当前问题】" + currentQuestion + "\n【当前回答】" + currentAnswer +
                "\n\n请基于以上完整的对话历史，给出下一步的追问。"));

        return callDeepSeekWithMessages(systemPrompt, messages);
    }

    /**
     * 生成面试评价
     */
    public String generateEvaluation(String[] questions, String[] answers) {
        StringBuilder conversation = new StringBuilder();
        for (int i = 0; i < questions.length; i++) {
            conversation.append(String.format("问题%d：%s\n", i + 1, questions[i]));
            if (i < answers.length) {
                conversation.append(String.format("回答%d：%s\n\n", i + 1, answers[i]));
            }
        }

        String systemPrompt =
                "你是一位专业的面试评估专家，请根据以下面试问答记录，生成一份面试评价报告。" +
                "报告包括：综合评分(0-100)、专业能力评价、表达能力评价、逻辑能力评价、优势分析、不足分析、改进建议。" +
                "请用简洁专业的语言，输出JSON格式。";

        String userMessage = "面试记录如下：\n\n" + conversation + "\n请生成评价报告。";
        return callDeepSeek(systemPrompt, userMessage);
    }

    /**
     * 构建消息列表（与 generateFollowUpWithHistory 逻辑一致）
     */
    private List<Object> buildMessages(String systemPrompt,
                                        List<Map<String, String>> history,
                                        String currentQuestion,
                                        String currentAnswer) {
        List<Object> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        if (history != null) {
            for (Map<String, String> qa : history) {
                messages.add(Map.of("role", "user", "content",
                        "【我的问题】" + qa.getOrDefault("question", "")));
                messages.add(Map.of("role", "assistant", "content",
                        "【候选人回答】" + qa.getOrDefault("answer", "")));
            }
        }
        messages.add(Map.of("role", "user", "content",
                "【当前问题】" + currentQuestion + "\n【当前回答】" + currentAnswer +
                "\n\n请基于以上完整的对话历史，给出下一步的追问。"));
        return messages;
    }

    // ========== 流式响应 ==========

    /**
     * 流式生成追问（异步）— 后台线程处理，不阻塞 Tomcat
     * <p>
     * DeepSeek 返回 SSE 格式流：
     * data: {"choices":[{"delta":{"content":"..."}}]}
     * data: [DONE]
     */
    public void streamGenerateFollowUp(SseEmitter emitter,
                                        String systemPrompt,
                                        List<Object> messages) {
        // 在后台线程执行流式读取，避免阻塞 Tomcat 请求线程
        CompletableFuture.runAsync(() -> doStreamGenerateFollowUp(emitter, messages));
    }

    /**
     * 实际执行流式读取（异步线程中运行）
     */
    private void doStreamGenerateFollowUp(SseEmitter emitter, List<Object> messages) {
        String url = apiUrl + "/chat/completions";
        HttpURLConnection connection = null;

        try {
            URI uri = new URI(url);
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setDoOutput(true);
            connection.setReadTimeout(60000);
            connection.setConnectTimeout(10000);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", 0.7);
            body.put("max_tokens", 2000);
            body.put("stream", true);

            // 发送请求
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = objectMapper.writeValueAsBytes(body);
                os.write(input);
                os.flush();
            }

            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                String errorBody = readStream(connection.getErrorStream());
                log.error("DeepSeek 流式API返回错误: status={}, body={}", statusCode, errorBody);
                try { emitter.send(SseEmitter.event().name("error").data("AI服务暂时不可用，请稍后重试")); } catch (IOException ignored) {}
                emitter.complete();
                return;
            }

            // 逐行读取 SSE 流
            StringBuilder fullContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();

                        if ("[DONE]".equals(data)) break;

                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> json = objectMapper.readValue(data, Map.class);
                            Object choicesObj = json.get("choices");
                            if (choicesObj instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> choices = (List<Map<String, Object>>) choicesObj;
                                if (!choices.isEmpty()) {
                                    Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                                    if (delta != null && delta.containsKey("content")) {
                                        String content = (String) delta.get("content");
                                        if (content != null && !content.isEmpty()) {
                                            fullContent.append(content);
                                            emitter.send(SseEmitter.event().name("delta").data(content));
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("解析SSE delta失败: {}", e.getMessage());
                        }
                    }
                }
            }

            // 发送完成事件
            emitter.send(SseEmitter.event().name("done").data(fullContent.toString()));
            emitter.complete();

        } catch (Exception e) {
            log.error("流式调用DeepSeek API失败: {}", e.getMessage());
            try { emitter.send(SseEmitter.event().name("error").data("AI服务异常，请稍后重试")); } catch (IOException ignored) {}
            emitter.completeWithError(e);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    /**
     * 流式生成追问（对外接口，兼容历史上下文）
     */
    public void streamGenerateFollowUpWithHistory(SseEmitter emitter,
                                                   List<Map<String, String>> history,
                                                   String currentQuestion,
                                                   String currentAnswer) {
        String systemPrompt =
                "你是一位经验丰富的技术面试官，正在对候选人进行深度面试。\n\n" +
                "## 面试背景\n" +
                "这场面试已经进行了几轮对话，你需要参考前面的问答历史，\n" +
                "确保追问与前文连贯，不要重复问过的问题，要在已有基础上深入挖掘。\n\n" +
                "## 追问原则\n" +
                "- 如果候选人回答正确但不够深入，追问细节和原理\n" +
                "- 如果候选人回答模糊，追问具体实现和案例\n" +
                "- 如果候选人回答错误，给出提示让他思考\n" +
                "- 追问要自然，像真实面试对话\n" +
                "- 避免重复已问过的问题\n" +
                "- 如果某个话题已经问透了，可以自然过渡到相关新话题\n" +
                "- 只返回追问内容，不要有其他文字";

        List<Object> messages = buildMessages(systemPrompt, history, currentQuestion, currentAnswer);
        streamGenerateFollowUp(emitter, systemPrompt, messages);
    }

    /**
     * 流式生成问题（带 SSE）
     */
    public void streamGenerateQuestion(SseEmitter emitter, String jobName, String difficulty) {
        String difficultyDesc = switch (difficulty) {
            case "初级" -> "基础概念、入门知识、简单实践";
            case "中级" -> "深入原理、项目经验、常见问题解决";
            case "高级" -> "架构设计、性能优化、复杂场景处理";
            default -> "综合能力";
        };

        String systemPrompt = String.format(
                "你是一位拥有10年经验的%s技术面试官，正在对候选人进行%s级别面试。\n\n" +
                "## 要求\n" +
                "- 问题要贴近实际工作场景，不要问八股文\n" +
                "- %s\n" +
                "- 问题要能区分候选人的真实水平\n" +
                "- 只返回问题内容，不要有其他多余文字\n\n" +
                "请生成一个有深度的面试问题。",
                jobName, difficulty, difficultyDesc
        );

        List<Object> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", "请生成一个面试问题"));

        streamGenerateFollowUp(emitter, systemPrompt, messages);
    }

    /**
     * 读取 InputStream 为字符串
     */
    private String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
