package com.interview.interview.model;

import lombok.Data;

/**
 * 分数分布统计项（管理端仪表盘用）。
 */
@Data
public class ScoreDistribution {
    private String range;
    private Long count;
}
