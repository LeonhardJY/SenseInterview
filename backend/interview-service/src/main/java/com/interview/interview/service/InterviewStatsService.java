package com.interview.interview.service;

import com.interview.interview.model.JobStatItem;
import com.interview.interview.model.ScoreDistribution;
import com.interview.interview.model.TrendItem;

import java.util.List;

/**
 * 面试统计分析服务。
 * <p>
 * 所有统计均通过 SQL 分组聚合（GROUP BY / SUM CASE）在数据库侧完成，
 * 避免"全表加载到内存再用 Java Stream 聚合"的反模式（大数据量下会全表扫描 + GC 压力甚至 OOM）。
 */
public interface InterviewStatsService {

    /** 岗位热度 Top 10（按面试次数降序）。 */
    List<JobStatItem> getJobStats();

    /** 综合评分分布（0-59 / 60-69 / 70-79 / 80-89 / 90-100 五段）。 */
    List<ScoreDistribution> getScoreDistribution();

    /** 近 7 天面试趋势（缺失日期零填充，按日期升序）。 */
    List<TrendItem> getInterviewTrend();
}
