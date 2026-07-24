package com.interview.ai.service;

import org.springframework.stereotype.Service;

@Service
public class LlmService {

    /**
     * 生成面试问题
     * @param jobName 岗位名称
     * @param difficulty 难度等级
     * @return 面试问题
     */
    public String generateQuestion(String jobName, String difficulty) {
        // TODO: 实现LLM问题生成
        // 这里需要调用OpenAI或其他LLM服务
        return "请介绍一下你在" + jobName + "岗位上的核心技能？";
    }

    /**
     * 生成追问
     * @param question 当前问题
     * @param answer 用户回答
     * @return 追问内容
     */
    public String generateFollowUp(String question, String answer) {
        // TODO: 实现LLM追问生成
        return "能详细说明一下你提到的这个技术点吗？";
    }

    /**
     * 生成面试评价
     * @param questions 面试问题列表
     * @param answers 用户回答列表
     * @return 评价内容
     */
    public String generateEvaluation(String[] questions, String[] answers) {
        // TODO: 实现LLM评价生成
        return "面试评价内容";
    }
}