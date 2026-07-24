package com.interview.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("interview_answer")
public class InterviewAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private String answerText;

    private String audioUrl;

    private String videoUrl;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}