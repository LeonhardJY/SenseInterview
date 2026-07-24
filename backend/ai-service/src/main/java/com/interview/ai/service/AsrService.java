package com.interview.ai.service;

import org.springframework.stereotype.Service;

@Service
public class AsrService {

    /**
     * 语音转文本
     * @param audioUrl 音频文件URL
     * @return 识别后的文本
     */
    public String speechToText(String audioUrl) {
        // TODO: 实现ASR语音识别
        // 这里需要调用阿里云或其他ASR服务
        return "语音识别结果";
    }
}