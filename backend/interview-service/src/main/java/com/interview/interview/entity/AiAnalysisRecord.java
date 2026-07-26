package com.interview.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_analysis_record")
public class AiAnalysisRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private String analysisType;

    private String resultJson;

    private BigDecimal score;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}