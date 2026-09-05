package com.interview.interview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.interview.entity.InterviewTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface InterviewTaskMapper extends BaseMapper<InterviewTask> {

    /** 按岗位名分组统计面试次数（SQL 聚合，取代全表加载后内存分组），取 Top 10。 */
    @Select("SELECT job_name AS jobName, COUNT(*) AS cnt FROM interview_task "
            + "WHERE job_name IS NOT NULL GROUP BY job_name ORDER BY cnt DESC LIMIT 10")
    List<Map<String, Object>> countByJobName();

    /** 按日期分组统计指定时间之后的面试数（SQL 聚合，用于近 7 天趋势）。 */
    @Select("SELECT DATE(create_time) AS d, COUNT(*) AS cnt FROM interview_task "
            + "WHERE create_time >= #{start} GROUP BY DATE(create_time)")
    List<Map<String, Object>> countByDateSince(@Param("start") LocalDateTime start);
}