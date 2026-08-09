package com.interview.interview.service;

import com.interview.interview.entity.EvaluationReport;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JSON 解析健壮性测试：喂入真实场景中 LLM 可能返回的各种格式，
 * 统计成功解析出有效分数的比例，验证解析层的工程稳定性。
 */
public class ReportParseRobustnessTest {

    private EvaluationReport parse(String aiResponse) throws Exception {
        ReportGenerateService service = new ReportGenerateService(null);
        Method m = ReportGenerateService.class.getDeclaredMethod("parseEvaluation", Long.class, String.class);
        m.setAccessible(true);
        return (EvaluationReport) m.invoke(service, 1L, aiResponse);
    }

    @Test
    void jsonParseSuccessRate() throws Exception {
        // 模拟 LLM 返回的各种格式，覆盖常见与异常场景
        String[] cases = {
                // 1. 纯 JSON
                "{\"totalScore\":82,\"professionalScore\":80,\"communicationScore\":85,\"logicScore\":78,\"summary\":\"不错\",\"suggestion\":\"加强\"}",
                // 2. ```json 代码块包裹
                "```json\n{\"totalScore\":75,\"professionalScore\":70,\"communicationScore\":80,\"logicScore\":75,\"summary\":\"还行\",\"suggestion\":\"继续\"}\n```",
                // 3. 无语言标记代码块
                "```\n{\"totalScore\":60,\"professionalScore\":55,\"communicationScore\":65,\"logicScore\":60,\"summary\":\"一般\",\"suggestion\":\"多练\"}\n```",
                // 4. 前后有 AI 废话
                "好的，这是你的面试评价：\n{\"totalScore\":88,\"professionalScore\":90,\"communicationScore\":85,\"logicScore\":87,\"summary\":\"优秀\",\"suggestion\":\"保持\"}\n希望对你有帮助",
                // 5. 分数越界（>100 / 负数）
                "{\"totalScore\":150,\"professionalScore\":-10,\"communicationScore\":85,\"logicScore\":78,\"summary\":\"x\",\"suggestion\":\"y\"}",
                // 6. 缺字段
                "{\"totalScore\":70,\"professionalScore\":72}",
                // 7. 字段名大小写/类型异常
                "{\"totalScore\":\"90\",\"professionalScore\":\"90\",\"communicationScore\":85,\"logicScore\":75,\"summary\":\"ok\",\"suggestion\":\"ok\"}",
                // 8. 完全不是 JSON
                "很抱歉，我无法生成评价。",
                // 9. 空字符串
                "",
                // 10. 截断的 JSON
                "{\"totalScore\":80,\"professionalScore\":78,\"communicationScore\"",
                // 11. JSON 数组（错误结构）
                "[{\"totalScore\":80}]",
                // 12. 中文标点干扰
                "你的成绩是：{\"totalScore\"：88，\"professionalScore\"：85，\"communicationScore\"：90，\"logicScore\"：86}",
                // 13. markdown 标题 + 表格 + JSON
                "## 评价\n\n| 维度 | 分 |\n|---|---|\n| 专业 | 80 |\n\n{\"totalScore\":80,\"professionalScore\":80,\"communicationScore\":82,\"logicScore\":78,\"summary\":\"中\",\"suggestion\":\"继续\"}",
                // 14. 换行/空格异常
                "  \n  {\n\"totalScore\": 85, \"professionalScore\": 83,\n\"communicationScore\": 87\n}  \n",
                // 15. 只给分数不给文本
                "{\"totalScore\":65,\"professionalScore\":60,\"communicationScore\":68,\"logicScore\":62}"
        };

        int success = 0;
        int total = cases.length;
        for (int i = 0; i < total; i++) {
            EvaluationReport report = parse(cases[i]);
            boolean ok = report != null
                    && report.getTotalScore() != null
                    && report.getTotalScore().compareTo(BigDecimal.ZERO) >= 0
                    && report.getTotalScore().compareTo(BigDecimal.valueOf(100)) <= 0;
            // 越界分数被 clamp 到 0-100 也算成功（正确工程行为）
            if (ok) success++;
            System.out.printf("[%d/%d] %s | total=%s%n", i + 1, total, ok ? "OK" : "FAIL",
                    report == null ? "null" : report.getTotalScore());
        }

        System.out.printf("==== JSON 解析成功率: %d/%d = %.0f%% ====%n", success, total, success * 100.0 / total);
        assertTrue(success >= total * 0.9, "健壮解析层成功率应 >= 90%，实际 " + success + "/" + total);
    }
}
