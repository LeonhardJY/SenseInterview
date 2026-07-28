package com.interview.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 语音转文字服务 (ASR)
 * <p>
 * 当前使用浏览器原生 Web Speech API 实现语音转文字（前端），
 * 不需要后端 ASR 服务。 此类保留为占位方便后续扩展。
 */
@Slf4j
@Service
public class AsrService {

    /**
     * 语音转文本 — 由前端浏览器 Web Speech API 实现，此处不处理
     */
    public String speechToText(String audioUrl) {
        log.warn("ASR 后端服务已禁用，前端使用浏览器原生语音识别");
        return "";
    }
}
