package com.interview.interview.model;

import lombok.Data;

/**
 * 岗位热度统计项（管理端仪表盘用）。
 */
@Data
public class JobStatItem {
    private String jobName;
    private Long count;
}
