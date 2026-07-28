package com.interview.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 面部情绪分析服务
 * <p>
 * 调用 Python DeepFace 微服务进行面部情绪识别。
 * 如果 Python 服务不可用，返回默认值（不阻塞主流程）。
 */
@Slf4j
@Service
public class FaceAnalysisService {

    @Value("${ai.emotion.service-url:http://localhost:5000}")
    private String emotionServiceUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 分析面部情绪
     *
     * @param imageBase64 Base64 编码的图片数据
     * @return 情绪分析结果，包含 emotion、confidence、details
     */
    public EmotionResult analyze(String imageBase64) {
        // 参数校验
        if (imageBase64 == null || imageBase64.isEmpty()) {
            return EmotionResult.empty("无图片数据");
        }

        try {
            Map<String, String> body = new LinkedHashMap<>();
            body.put("image", imageBase64);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(emotionServiceUrl + "/analyze"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> result = objectMapper.readValue(
                        response.body(),
                        new TypeReference<Map<String, Object>>() {}
                );

                String emotion = (String) result.getOrDefault("emotion", "unknown");
                double confidence = ((Number) result.getOrDefault("confidence", 0)).doubleValue();

                @SuppressWarnings("unchecked")
                Map<String, Object> details = (Map<String, Object>) result.getOrDefault("details", Map.of());

                log.debug("情绪识别结果: {}, 置信度: {}", emotion, confidence);
                return new EmotionResult(emotion, confidence, details, null);
            } else {
                log.warn("情绪服务返回异常状态码: {}", response.statusCode());
                return EmotionResult.empty("服务异常");
            }

        } catch (java.net.ConnectException e) {
            log.warn("情绪服务未启动 ({}), 请运行: python backend/emotion-service/app.py", emotionServiceUrl);
            return EmotionResult.empty("服务未连接");
        } catch (Exception e) {
            log.error("情绪分析调用失败: {}", e.getMessage());
            return EmotionResult.empty("调用失败");
        }
    }

    /**
     * 健康检查
     */
    public boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(emotionServiceUrl + "/health"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // ========== 结果模型 ==========

    public record EmotionResult(
            String emotion,
            double confidence,
            Map<String, Object> details,
            String error
    ) {
        public static EmotionResult empty(String error) {
            return new EmotionResult("unknown", 0, Map.of(), error);
        }

        public boolean isFaceDetected() {
            return !"none".equals(emotion) && !"unknown".equals(emotion);
        }

        public String toDisplayText() {
            return switch (emotion) {
                case "happy" -> "😊 自信";
                case "sad" -> "😔 低落";
                case "angry" -> "😠 紧张";
                case "surprise" -> "😮 惊讶";
                case "fear" -> "😨 紧张";
                case "disgust" -> "😣 不适";
                case "neutral" -> "😐 平静";
                default -> "❓ 未知";
            };
        }
    }
}
