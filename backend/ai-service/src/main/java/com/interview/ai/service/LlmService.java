package com.interview.ai.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LlmService {

    // 模拟问题库
    private static final Map<String, List<String>> QUESTION_BANK = new HashMap<>();

    static {
        // Java开发问题
        QUESTION_BANK.put("Java开发工程师", Arrays.asList(
                "请介绍一下Java中的多态性是什么？",
                "HashMap和ConcurrentHashMap的区别是什么？",
                "请解释一下JVM的内存模型和垃圾回收机制。",
                "Spring Boot的自动配置原理是什么？",
                "请介绍一下你对微服务架构的理解。",
                "如何优化SQL查询性能？",
                "请解释一下设计模式中的单例模式，有哪些实现方式？",
                "Redis的常用数据结构有哪些？各自的应用场景是什么？"
        ));

        // 前端开发问题
        QUESTION_BANK.put("前端开发工程师", Arrays.asList(
                "请介绍一下Vue3的Composition API相比Options API有什么优势？",
                "React和Vue的区别是什么？各自的特点？",
                "请解释一下JavaScript的事件循环机制。",
                "如何优化前端页面加载性能？",
                "请介绍一下CSS的盒模型和BFC。",
                "什么是虚拟DOM？它的工作原理是什么？",
                "请介绍一下TypeScript的类型系统。",
                "如何实现前端路由？"
        ));

        // Python开发问题
        QUESTION_BANK.put("Python开发工程师", Arrays.asList(
                "请介绍一下Python的GIL是什么？它对多线程有什么影响？",
                "Django和Flask的区别是什么？",
                "请解释一下Python的装饰器是什么？如何实现？",
                "Python中列表和元组的区别是什么？",
                "请介绍一下Python的内存管理机制。",
                "如何优化Python代码的性能？",
                "请介绍一下Python的上下文管理器。",
                "Django的ORM是如何工作的？"
        ));

        // 产品经理问题
        QUESTION_BANK.put("产品经理", Arrays.asList(
                "请介绍一下你如何进行需求分析？",
                "如何制定产品路线图？",
                "请介绍一下敏捷开发流程。",
                "如何进行用户调研？",
                "如何衡量产品的成功？",
                "请介绍一下MVP（最小可行产品）的概念。",
                "如何处理产品需求的优先级？",
                "请介绍一下你使用过的产品分析工具。"
        ));

        // 数据分析问题
        QUESTION_BANK.put("数据分析师", Arrays.asList(
                "请介绍一下数据分析的基本流程。",
                "SQL中JOIN有哪些类型？各自的应用场景？",
                "如何进行数据清洗？",
                "请介绍一下常用的统计分析方法。",
                "如何进行数据可视化？",
                "请介绍一下A/B测试的原理。",
                "如何处理缺失值？",
                "请介绍一下机器学习的基本概念。"
        ));

        // 测试工程师问题
        QUESTION_BANK.put("测试工程师", Arrays.asList(
                "请介绍一下软件测试的生命周期。",
                "黑盒测试和白盒测试的区别是什么？",
                "如何编写测试用例？",
                "请介绍一下自动化测试框架。",
                "如何进行性能测试？",
                "请介绍一下持续集成和持续部署。",
                "如何进行接口测试？",
                "请介绍一下测试驱动开发（TDD）。"
        ));
    }

    // 模拟追问库
    private static final List<String> FOLLOW_UP_QUESTIONS = Arrays.asList(
            "能详细说明一下你提到的这个技术点吗？",
            "你在实际项目中是如何应用这个技术的？",
            "这个技术有什么优缺点？",
            "有没有遇到过相关的问题？是如何解决的？",
            "你认为这个技术的未来发展趋势是什么？",
            "能举一个具体的例子吗？",
            "这个技术和其他类似技术相比有什么优势？",
            "你在学习这个技术的过程中有什么心得？"
    );

    // 模拟评价模板
    private static final String[] EVALUATION_TEMPLATES = {
            "面试表现良好，技术基础扎实，表达清晰。",
            "面试表现一般，部分知识点需要加强。",
            "面试表现优秀，对技术有深入的理解。",
            "面试表现需要改进，建议加强基础知识学习。",
            "面试表现中等，有一定的实践经验。"
    };

    /**
     * 生成面试问题
     * @param jobName 岗位名称
     * @param difficulty 难度等级
     * @return 面试问题
     */
    public String generateQuestion(String jobName, String difficulty) {
        List<String> questions = QUESTION_BANK.get(jobName);
        if (questions == null || questions.isEmpty()) {
            questions = QUESTION_BANK.get("Java开发工程师");
        }
        Random random = new Random();
        return questions.get(random.nextInt(questions.size()));
    }

    /**
     * 生成追问
     * @param question 当前问题
     * @param answer 用户回答
     * @return 追问内容
     */
    public String generateFollowUp(String question, String answer) {
        Random random = new Random();
        return FOLLOW_UP_QUESTIONS.get(random.nextInt(FOLLOW_UP_QUESTIONS.size()));
    }

    /**
     * 生成面试评价
     * @param questions 面试问题列表
     * @param answers 用户回答列表
     * @return 评价内容
     */
    public String generateEvaluation(String[] questions, String[] answers) {
        Random random = new Random();
        return EVALUATION_TEMPLATES[random.nextInt(EVALUATION_TEMPLATES.length)];
    }
}