package com.interview.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.interview.entity.EvaluationReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface EvaluationReportMapper extends BaseMapper<EvaluationReport> {

    /**
     * 一次性 SQL 聚合出 5 个分数段人数（取代全表加载后内存分桶）。
     * b0:[0,60) b1:[60,70) b2:[70,80) b3:[80,90) b4:[90,100]
     */
    @Select("SELECT "
            + "SUM(CASE WHEN total_score < 60 THEN 1 ELSE 0 END) AS b0, "
            + "SUM(CASE WHEN total_score >= 60 AND total_score < 70 THEN 1 ELSE 0 END) AS b1, "
            + "SUM(CASE WHEN total_score >= 70 AND total_score < 80 THEN 1 ELSE 0 END) AS b2, "
            + "SUM(CASE WHEN total_score >= 80 AND total_score < 90 THEN 1 ELSE 0 END) AS b3, "
            + "SUM(CASE WHEN total_score >= 90 THEN 1 ELSE 0 END) AS b4 "
            + "FROM evaluation_report WHERE total_score IS NOT NULL")
    Map<String, Object> countScoreBuckets();
}