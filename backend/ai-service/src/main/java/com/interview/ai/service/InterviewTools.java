package com.interview.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Agent 面试官可调用的工具集：
 * 1. 检索大厂面试知识库（RAG，带来源标注）
 * 2. 查询系统题库（HTTP 调用 question-service，带分类/难度标注）
 */
@Slf4j
@Component
public class InterviewTools {

    private final RagService ragService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.question-service-url:http://localhost:8083}")
    private String questionServiceUrl;

    public InterviewTools(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * RAG 检索：从大厂面试知识库中取与关键词相关的面试真题
     * 每条结果带【来源：公司名】标注，便于 Agent 出题时引用出处
     */
    @Tool("检索大厂面试知识库，返回与关键词相关的面试真题（每条标注来源公司，含答案要点）")
    public String searchKnowledgeBase(String keyword) {
        List<RagService.RagDoc> docs = ragService.retrieveDocs(keyword);
        if (docs.isEmpty()) {
            return "知识库中没有找到与「" + keyword + "」相关的题目。";
        }
        StringBuilder sb = new StringBuilder();
        for (RagService.RagDoc doc : docs) {
            String source = doc.sourceName();
            sb.append(source.isEmpty() ? "" : "【来源：" + source + "】\n");
            sb.append(doc.text()).append("\n\n---\n\n");
        }
        return sb.toString();
    }

    /**
     * 系统题库检索：按关键词查询题库表，返回格式化题目列表
     */
    @Tool("从系统题库中查询与关键词相关的面试题，返回题目及参考答案要点")
    public String searchQuestionBank(String keyword) {
        try {
            String url = questionServiceUrl + "/api/question/search?keyword=" + keyword;
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

            JsonNode root = response.getBody();
            if (root == null || !root.has("data") || root.get("data").isEmpty()) {
                return "题库中没有找到与「" + keyword + "」相关的题目。";
            }

            StringBuilder sb = new StringBuilder();
            for (JsonNode q : root.get("data")) {
                sb.append("题目：").append(q.path("title").asText("")).append("\n");
                if (q.has("category") && !q.path("category").asText().isEmpty()) {
                    sb.append("分类：").append(q.path("category").asText()).append(" · ");
                }
                if (q.has("level") && !q.path("level").asText().isEmpty()) {
                    sb.append("难度：").append(q.path("level").asText());
                }
                sb.append("\n答案要点：").append(q.path("answer").asText("")).append("\n\n---\n\n");
            }
            return sb.toString().trim();
        } catch (Exception e) {
            log.error("调用题库服务失败: {}", e.getMessage());
            return "题库服务暂时不可用，请稍后重试。";
        }
    }

    /**
     * 面试环境信息（演示工具，展示 Agent 多工具编排）
     */
    @Tool("获取当前面试的设置信息")
    public String getInterviewEnvironment() {
        return "当前面试类型：技术模拟面试；领域：后端开发（Java）；"
                + "默认时长 40 分钟；出题范围：JVM、并发、集合、MySQL、Redis、系统设计。";
    }
}
