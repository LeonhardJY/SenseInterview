package com.interview.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 语音转文字服务 (ASR)
 * <p>
 * 集成 OpenAI Whisper API 实现语音转文字。
 * 支持直接上传音频文件或传入文件路径。
 */
@Slf4j
@Service
public class AsrService {

    @Value("${ai.model.asr.api-key}")
    private String apiKey;

    @Value("${ai.model.asr.api-url}")
    private String apiUrl;

    @Value("${ai.model.asr.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 语音转文本 — 从上传的音频文件直接转写
     *
     * @param file 上传的音频文件 (支持 mp3, wav, m4a, ogg, webm 等格式)
     * @return 识别后的文本
     */
    public String speechToText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("音频文件为空");
            return "";
        }

        try {
            String originalFilename = file.getOriginalFilename();
            byte[] audioBytes = file.getBytes();

            log.info("开始语音识别, 文件: {}, 大小: {} bytes", originalFilename, audioBytes.length);
            return callWhisperApi(audioBytes, originalFilename);
        } catch (IOException e) {
            log.error("读取音频文件失败: {}", e.getMessage());
            return "语音识别失败：文件读取错误";
        }
    }

    /**
     * 语音转文本 — 从文件路径读取（兼容旧接口）
     *
     * @param audioUrl 音频文件 URL 或本地路径
     * @return 识别后的文本
     */
    public String speechToText(String audioUrl) {
        log.warn("speechToText(String) 已废弃，请使用 speechToText(MultipartFile) 直接上传");
        return "语音识别结果（请使用上传接口）";
    }

    /**
     * 调用 OpenAI Whisper REST API
     */
    private String callWhisperApi(byte[] audioBytes, String filename) {
        String url = apiUrl + "/v1/audio/transcriptions";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 构建 multipart 请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("model", model);
        body.add("file", new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return filename != null ? filename : "audio.wav";
            }
        });
        body.add("language", "zh");
        body.add("response_format", "json");

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("text")) {
                String text = (String) responseBody.get("text");
                log.info("语音识别成功, 文本长度: {}", text != null ? text.length() : 0);
                return text != null ? text.trim() : "";
            }

            log.warn("Whisper API 返回格式异常: {}", responseBody);
            return "语音识别失败：服务返回异常";
        } catch (Exception e) {
            log.error("调用 Whisper API 失败: {}", e.getMessage());
            return "语音识别失败：服务调用异常";
        }
    }
}
