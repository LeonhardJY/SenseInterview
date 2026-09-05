package com.interview.interview.service.impl;

import com.interview.interview.mapper.EvaluationReportMapper;
import com.interview.interview.mapper.InterviewTaskMapper;
import com.interview.interview.model.JobStatItem;
import com.interview.interview.model.ScoreDistribution;
import com.interview.interview.model.TrendItem;
import com.interview.interview.service.InterviewStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 面试统计分析服务实现。
 * <p>
 * 聚合均下推到 SQL（GROUP BY / SUM CASE），服务层只负责把聚合结果映射为 VO、
 * 补齐分数段标签与近 7 天零填充，不再把整表加载进内存。
 */
@Service
@RequiredArgsConstructor
public class InterviewStatsServiceImpl implements InterviewStatsService {

    private static final String[] SCORE_LABELS = {"0-59", "60-69", "70-79", "80-89", "90-100"};
    private static final String[] SCORE_BUCKET_KEYS = {"b0", "b1", "b2", "b3", "b4"};

    private final InterviewTaskMapper taskMapper;
    private final EvaluationReportMapper reportMapper;

    @Override
    public List<JobStatItem> getJobStats() {
        List<Map<String, Object>> rows = taskMapper.countByJobName();
        List<JobStatItem> result = new ArrayList<>();
        if (rows == null) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            JobStatItem item = new JobStatItem();
            item.setJobName(toStr(row.get("jobName")));
            item.setCount(toLong(row.get("cnt")));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<ScoreDistribution> getScoreDistribution() {
        Map<String, Object> row = reportMapper.countScoreBuckets();
        List<ScoreDistribution> result = new ArrayList<>();
        for (int i = 0; i < SCORE_LABELS.length; i++) {
            ScoreDistribution item = new ScoreDistribution();
            item.setRange(SCORE_LABELS[i]);
            item.setCount(row == null ? 0L : toLong(row.get(SCORE_BUCKET_KEYS[i])));
            result.add(item);
        }
        return result;
    }

    @Override
    public List<TrendItem> getInterviewTrend() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(6).atStartOfDay();

        List<Map<String, Object>> rows = taskMapper.countByDateSince(start);
        Map<String, Long> countByDate = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                countByDate.put(toStr(row.get("d")), toLong(row.get("cnt")));
            }
        }

        // 按日期升序补齐近 7 天（今天-6 → 今天），SQL 未返回的日期计为 0
        List<TrendItem> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            String date = today.minusDays(i).toString();
            TrendItem item = new TrendItem();
            item.setDate(date);
            item.setCount(countByDate.getOrDefault(date, 0L));
            trend.add(item);
        }
        return trend;
    }

    /** DATE()/字符串列 → 统一的 yyyy-MM-dd 字符串（兼容 java.sql.Date / LocalDate / String）。 */
    private static String toStr(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    /** COUNT/SUM 列 → long（兼容 Long / BigInteger / BigDecimal；NULL 计为 0）。 */
    private static long toLong(Object o) {
        return o instanceof Number n ? n.longValue() : 0L;
    }
}
