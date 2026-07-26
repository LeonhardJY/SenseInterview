package com.interview.interview.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("hot_question")
public class HotQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String count;

    private Integer sortOrder;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}