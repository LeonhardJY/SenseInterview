package com.interview.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("evaluation_report")
public class EvaluationReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private BigDecimal totalScore;

    private Integer professionalScore;

    private Integer communicationScore;

    private Integer logicScore;

    private String summary;

    private String suggestion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}