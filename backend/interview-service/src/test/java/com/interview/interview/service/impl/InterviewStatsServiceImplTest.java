package com.interview.interview.service.impl;

import com.interview.interview.mapper.EvaluationReportMapper;
import com.interview.interview.mapper.InterviewTaskMapper;
import com.interview.interview.model.JobStatItem;
import com.interview.interview.model.ScoreDistribution;
import com.interview.interview.model.TrendItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 面试统计服务单元测试。
 * <p>
 * 重点验证：统计不再走"全表 list() + 内存聚合"，而是委托 SQL 分组聚合查询，
 * 并正确处理结果的映射、标签、近 7 天零填充与空数据边界。
 */
@ExtendWith(MockitoExtension.class)
class InterviewStatsServiceImplTest {

    @Mock
    private InterviewTaskMapper taskMapper;
    @Mock
    private EvaluationReportMapper reportMapper;

    private InterviewStatsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InterviewStatsServiceImpl(taskMapper, reportMapper);
    }

    @Test
    void getJobStats_委托SQL分组聚合并映射为Top岗位列表() {
        when(taskMapper.countByJobName()).thenReturn(List.of(
                row("jobName", "Java开发工程师", "cnt", 12L),
                row("jobName", "前端开发工程师", "cnt", 5L)
        ));

        List<JobStatItem> stats = service.getJobStats();

        assertThat(stats).hasSize(2);
        assertThat(stats.get(0).getJobName()).isEqualTo("Java开发工程师");
        assertThat(stats.get(0).getCount()).isEqualTo(12L);
        assertThat(stats.get(1).getJobName()).isEqualTo("前端开发工程师");
        assertThat(stats.get(1).getCount()).isEqualTo(5L);
        // 关键：走 SQL 聚合查询，而非全表加载
        verify(taskMapper).countByJobName();
    }

    @Test
    void getScoreDistribution_将SQL五个分桶计数映射为固定标签区间() {
        when(reportMapper.countScoreBuckets()).thenReturn(
                buckets(2L, 3L, 1L, 0L, 4L));

        List<ScoreDistribution> dist = service.getScoreDistribution();

        assertThat(dist).hasSize(5);
        assertThat(dist.get(0).getRange()).isEqualTo("0-59");
        assertThat(dist.get(0).getCount()).isEqualTo(2L);
        assertThat(dist.get(1).getRange()).isEqualTo("60-69");
        assertThat(dist.get(2).getRange()).isEqualTo("70-79");
        assertThat(dist.get(3).getRange()).isEqualTo("80-89");
        assertThat(dist.get(4).getRange()).isEqualTo("90-100");
        assertThat(dist.get(4).getCount()).isEqualTo(4L);
        verify(reportMapper).countScoreBuckets();
    }

    @Test
    void getScoreDistribution_空表时SUM返回NULL_各区间计为0() {
        when(reportMapper.countScoreBuckets()).thenReturn(buckets(null, null, null, null, null));

        List<ScoreDistribution> dist = service.getScoreDistribution();

        assertThat(dist).hasSize(5);
        assertThat(dist).allSatisfy(d -> assertThat(d.getCount()).isEqualTo(0L));
    }

    @Test
    void getInterviewTrend_用SQL按日聚合并对近7天缺失日期零填充() {
        LocalDate today = LocalDate.now();
        when(taskMapper.countByDateSince(any())).thenReturn(List.of(
                row("d", today.toString(), "cnt", 5L),
                row("d", today.minusDays(2).toString(), "cnt", 3L)
        ));

        List<TrendItem> trend = service.getInterviewTrend();

        assertThat(trend).hasSize(7);
        // 序列按时间升序：索引 0 = 今天-6，索引 6 = 今天
        assertThat(trend.get(0).getDate()).isEqualTo(today.minusDays(6).toString());
        assertThat(trend.get(6).getDate()).isEqualTo(today.toString());
        assertThat(trend.get(6).getCount()).isEqualTo(5L);   // 今天
        assertThat(trend.get(4).getCount()).isEqualTo(3L);   // 今天-2
        assertThat(trend.get(5).getCount()).isEqualTo(0L);   // 今天-1，缺失→零填充
        assertThat(trend.get(0).getCount()).isEqualTo(0L);   // 今天-6，缺失→零填充
        verify(taskMapper).countByDateSince(any());
    }

    // ========== 测试辅助 ==========

    private static Map<String, Object> row(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> m = new HashMap<>();
        m.put(k1, v1);
        m.put(k2, v2);
        return m;
    }

    private static Map<String, Object> buckets(Long b0, Long b1, Long b2, Long b3, Long b4) {
        Map<String, Object> m = new HashMap<>();
        m.put("b0", b0);
        m.put("b1", b1);
        m.put("b2", b2);
        m.put("b3", b3);
        m.put("b4", b4);
        return m;
    }
}
