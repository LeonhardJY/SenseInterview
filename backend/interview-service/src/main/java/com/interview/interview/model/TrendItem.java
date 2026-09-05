package com.interview.interview.model;

import lombok.Data;

/**
 * 面试趋势统计项（近 7 天，管理端仪表盘用）。
 */
@Data
public class TrendItem {
    private String date;
    private Long count;
}
