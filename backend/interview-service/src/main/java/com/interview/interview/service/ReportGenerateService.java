package com.interview.interview.service;

import com.interview.interview.controller.InterviewTaskController.GenerateReportRequest.QaItem;
import com.interview.interview.entity.EvaluationReport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGenerateService {

    private final EvaluationReportService evaluationReportService;

    @Value("${ai.model.llm.api-url}")
    private String apiUrl;

    @Value("${ai.model.llm.api-key}")
    private String apiKey;

    @Value("${ai.model.llm.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成面试报告
     */
    public void generateReport(Long taskId, List<QaItem> qaList) {
        if (taskId == null || qaList == null || qaList.isEmpty()) {
            log.warn("生成报告参数无效: taskId={}, qaList.size={}", taskId, qaList == null ? 0 : qaList.size());
            return;
        }

        // 1. 构建问答记录文本
        StringBuilder conversation = new StringBuilder();
        for (int i = 0; i < qaList.size(); i++) {
            QaItem qa = qaList.get(i);
            if (qa == null || qa.getQuestion() == null) continue;
            conversation.append(String.format("问题%d：%s\n", i + 1, qa.getQuestion()));
            conversation.append(String.format("回答%d：%s\n\n", i + 1,
                    qa.getAnswer() != null ? qa.getAnswer() : "(未作答)"));
        }

        // 2. 调用AI生成评价
        String evaluationJson = callAiForEvaluation(conversation.toString());

        // 3. 解析AI返回的评价
        EvaluationReport report = parseEvaluation(taskId, evaluationJson);

        // 4. 保存/更新报告（避免重复生成时唯一键冲突）
        try {
            EvaluationReport existing = evaluationReportService.findByTaskId(taskId);
            if (existing != null) {
                report.setId(existing.getId());
                evaluationReportService.updateById(report);
            } else {
                evaluationReportService.save(report);
            }
            log.info("面试报告已生成，taskId: {}, totalScore: {}", taskId, report.getTotalScore());
        } catch (Exception e) {
            log.error("保存面试报告失败: taskId={}, error={}", taskId, e.getMessage());
        }
    }

    /**
     * 调用AI生成评价
     */
    private String callAiForEvaluation(String conversation) {
        String url = apiUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String systemPrompt = "你是一位拥有10年技术面试经验的资深技术面试官。\n\n" +
                "## 评分标准（严格把控，不要给虚高分数）\n" +
                "- **专业能力（0-100）**：技术知识是否扎实、是否有实际经验、概念是否准确\n" +
                "- **表达能力（0-100）**：回答是否条理清晰、能否简洁明了地表达技术概念\n" +
                "- **逻辑能力（0-100）**：分析问题是否有条理、思路是否清晰、是否有逻辑漏洞\n\n" +
                "## 综合评分参考\n" +
                "- 90-100：优秀\n" +
                "- 75-89：良好\n" +
                "- 60-74：一般\n" +
                "- 60以下：不合格\n\n" +
                "## 输出要求\n" +
                "请严格只返回以下JSON格式，不要有任何其他文字、markdown标记或解释：\n" +
                "{\n" +
                "  \"totalScore\": 75,\n" +
                "  \"professionalScore\": 70,\n" +
                "  \"communicationScore\": 80,\n" +
                "  \"logicScore\": 75,\n" +
                "  \"summary\": \"200字左右的面试总结\",\n" +
                "  \"suggestion\": \"100字左右的具体改进建议\"\n" +
                "}\n\n" +
                "注意：分数要客观真实，不要虚高。";

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", new Object[]{
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", "面试记录如下：\n\n" + conversation + "\n请生成评价报告。")
        });
        body.put("temperature", 0.3);
        body.put("max_tokens", 1000);

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
            log.warn("AI 返回为空，使用默认评价");
            return getDefaultEvaluation();
        } catch (Exception e) {
            log.error("调用AI生成评价失败: {}", e.getMessage());
            return getDefaultEvaluation();
        }
    }

    /**
     * 解析AI返回的评价JSON（健壮版）
     * 处理：纯JSON、markdown包裹、多余文本等场景
     */
    private EvaluationReport parseEvaluation(Long taskId, String json) {
        EvaluationReport report = new EvaluationReport();
        report.setTaskId(taskId);

        try {
            String jsonStr = extractJson(json);
            JsonNode node = objectMapper.readTree(jsonStr);

            report.setTotalScore(clampScore(node, "totalScore", BigDecimal.valueOf(75)));
            report.setProfessionalScore(clampIntScore(node, "professionalScore", 75));
            report.setCommunicationScore(clampIntScore(node, "communicationScore", 75));
            report.setLogicScore(clampIntScore(node, "logicScore", 75));
            report.setSummary(node.has("summary") ? node.get("summary").asText("") :
                    "面试已完成");
            report.setSuggestion(node.has("suggestion") ? node.get("suggestion").asText("") :
                    "继续努力");
        } catch (Exception e) {
            log.error("解析评价JSON失败, 原始文本: {}, 错误: {}", json, e.getMessage());
            applyDefaultScores(report);
        }

        return report;
    }

    /**
     * 从 AI 响应中提取 JSON 字符串
     * 支持：纯JSON、```json...```包裹、前后有多余文本
     */
    private String extractJson(String text) {
        if (text == null || text.isBlank()) return getDefaultEvaluation();

        String trimmed = text.trim();

        // 尝试直接解析（AI 可能直接返回纯 JSON）
        try {
            objectMapper.readTree(trimmed);
            return trimmed;
        } catch (Exception ignored) {}

        // 尝试提取 ```json ... ``` 块
        int jsonStart = trimmed.indexOf("```json");
        if (jsonStart >= 0) {
            int jsonEnd = trimmed.indexOf("```", jsonStart + 7);
            if (jsonEnd > jsonStart) {
                return trimmed.substring(jsonStart + 7, jsonEnd).trim();
            }
        }

        // 尝试提取 ``` ... ``` 块（无 language tag）
        jsonStart = trimmed.indexOf("```");
        if (jsonStart >= 0) {
            int jsonEnd = trimmed.indexOf("```", jsonStart + 3);
            if (jsonEnd > jsonStart) {
                return trimmed.substring(jsonStart + 3, jsonEnd).trim();
            }
        }

        // 尝试找到第一个 { 和最后一个 } 之间的内容
        int braceStart = trimmed.indexOf('{');
        int braceEnd = trimmed.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            String candidate = trimmed.substring(braceStart, braceEnd + 1);
            try {
                objectMapper.readTree(candidate);
                return candidate;
            } catch (Exception ignored) {}
        }

        log.warn("无法从AI响应中提取JSON，使用默认评价。原始响应: {}", text);
        return getDefaultEvaluation();
    }

    private BigDecimal clampScore(JsonNode node, String field, BigDecimal defaultVal) {
        if (!node.has(field)) return defaultVal;
        try {
            int score = node.get(field).asInt();
            return BigDecimal.valueOf(Math.max(0, Math.min(100, score)));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private int clampIntScore(JsonNode node, String field, int defaultVal) {
        if (!node.has(field)) return defaultVal;
        try {
            return Math.max(0, Math.min(100, node.get(field).asInt()));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private void applyDefaultScores(EvaluationReport report) {
        report.setTotalScore(BigDecimal.valueOf(75));
        report.setProfessionalScore(75);
        report.setCommunicationScore(75);
        report.setLogicScore(75);
        report.setSummary("面试已完成，AI评分解析失败，显示默认分数。");
        report.setSuggestion("建议重新生成报告以获取准确评分。");
    }

    /**
     * 默认评价（AI调用失败时使用）
     */
    private String getDefaultEvaluation() {
        return "{\"totalScore\":75,\"professionalScore\":75,\"communicationScore\":75,\"logicScore\":75,\"summary\":\"面试已完成\",\"suggestion\":\"继续努力\"}";
    }
}
