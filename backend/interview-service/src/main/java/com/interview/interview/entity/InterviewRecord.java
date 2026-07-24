package com.interview.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_record")
public class InterviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    private Integer roundNum;

    private String question;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}