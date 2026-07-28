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
        // 1. 构建问答记录文本
        StringBuilder conversation = new StringBuilder();
        for (int i = 0; i < qaList.size(); i++) {
            QaItem qa = qaList.get(i);
            conversation.append(String.format("问题%d：%s\n", i + 1, qa.getQuestion()));
            conversation.append(String.format("回答%d：%s\n\n", i + 1, qa.getAnswer()));
        }

        // 2. 调用AI生成评价
        String evaluationJson = callAiForEvaluation(conversation.toString());

        // 3. 解析AI返回的评价
        EvaluationReport report = parseEvaluation(taskId, evaluationJson);

        // 4. 保存/更新报告（避免重复生成时唯一键冲突）
        EvaluationReport existing = evaluationReportService.findByTaskId(taskId);
        if (existing != null) {
            report.setId(existing.getId());
            evaluationReportService.updateById(report);
        } else {
            evaluationReportService.save(report);
        }
        log.info("面试报告已生成，taskId: {}", taskId);
    }

    /**
     * 调用AI生成评价
     */
    private String callAiForEvaluation(String conversation) {
        String url = apiUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String systemPrompt = "你是一位拥有10年技术面试经验的资深技术面试官，曾在BAT等一线大厂担任过高级技术面试官。\n\n" +
                "## 你的面试风格\n" +
                "- 严格、专业、不放过任何细节\n" +
                "- 注重候选人的实际项目经验，而非纸上谈兵\n" +
                "- 关注候选人的技术深度和广度\n" +
                "- 重视问题分析能力和解决问题的思路\n" +
                "- 对技术概念的准确性要求很高\n\n" +
                "## 评分标准（严格把控，不要给虚高分数）\n" +
                "- **专业能力（0-100）**：技术知识是否扎实、是否有实际经验、概念是否准确\n" +
                "- **表达能力（0-100）**：回答是否条理清晰、能否简洁明了地表达技术概念\n" +
                "- **逻辑能力（0-100）**：分析问题是否有条理、思路是否清晰、是否有逻辑漏洞\n\n" +
                "## 综合评分计算\n" +
                "- 90-100：优秀，技术扎实，可直接录用\n" +
                "- 75-89：良好，有一定基础，可培养\n" +
                "- 60-74：一般，基础薄弱，需要加强学习\n" +
                "- 60以下：不合格，技术能力不达标\n\n" +
                "## 输出要求\n" +
                "请严格按照以下JSON格式返回，不要有任何其他文字：\n" +
                "{\n" +
                "  \"totalScore\": 75,\n" +
                "  \"professionalScore\": 70,\n" +
                "  \"communicationScore\": 80,\n" +
                "  \"logicScore\": 75,\n" +
                "  \"summary\": \"200字左右的面试总结，指出主要优点和问题\",\n" +
                "  \"suggestion\": \"100字左右的具体改进建议\"\n" +
                "}\n\n" +
                "注意：分数要客观真实，不要为了好看而虚高。如果回答不好，该给低分就给低分。";

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", new Object[]{
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", "面试记录如下：\n\n" + conversation + "\n请生成评价报告。")
        });
        body.put("temperature", 0.7);
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
            return getDefaultEvaluation();
        } catch (Exception e) {
            log.error("调用AI生成评价失败: {}", e.getMessage());
            return getDefaultEvaluation();
        }
    }

    /**
     * 解析AI返回的评价JSON
     */
    private EvaluationReport parseEvaluation(Long taskId, String json) {
        EvaluationReport report = new EvaluationReport();
        report.setTaskId(taskId);

        try {
            // 提取JSON部分（AI可能返回带markdown格式的JSON）
            String jsonStr = json;
            if (json.contains("```json")) {
                jsonStr = json.substring(json.indexOf("```json") + 7, json.indexOf("```"));
            } else if (json.contains("```")) {
                jsonStr = json.substring(json.indexOf("```") + 3, json.lastIndexOf("```"));
            }

            JsonNode node = objectMapper.readTree(jsonStr.trim());
            report.setTotalScore(node.has("totalScore") ? BigDecimal.valueOf(node.get("totalScore").asInt()) : BigDecimal.valueOf(75));
            report.setProfessionalScore(node.has("professionalScore") ? node.get("professionalScore").asInt() : 75);
            report.setCommunicationScore(node.has("communicationScore") ? node.get("communicationScore").asInt() : 75);
            report.setLogicScore(node.has("logicScore") ? node.get("logicScore").asInt() : 75);
            report.setSummary(node.has("summary") ? node.get("summary").asText() : "面试已完成");
            report.setSuggestion(node.has("suggestion") ? node.get("suggestion").asText() : "继续努力");
        } catch (Exception e) {
            log.error("解析评价JSON失败: {}", e.getMessage());
            report.setTotalScore(BigDecimal.valueOf(75));
            report.setProfessionalScore(75);
            report.setCommunicationScore(75);
            report.setLogicScore(75);
            report.setSummary("面试已完成，AI正在分析您的表现...");
            report.setSuggestion("继续努力，不断提升自己的技能。");
        }

        return report;
    }

    /**
     * 默认评价（AI调用失败时使用）
     */
    private String getDefaultEvaluation() {
        return "{\"totalScore\":75,\"professionalScore\":75,\"communicationScore\":75,\"logicScore\":75,\"summary\":\"面试已完成\",\"suggestion\":\"继续努力\"}";
    }
}
