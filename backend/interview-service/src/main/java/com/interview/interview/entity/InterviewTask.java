package com.interview.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_task")
public class InterviewTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String jobName;

    private String mode;

    private String difficulty;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}