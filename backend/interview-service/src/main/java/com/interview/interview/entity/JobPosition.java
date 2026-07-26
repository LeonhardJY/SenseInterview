package com.interview.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("job_position")
public class JobPosition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String category;

    private String level;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}