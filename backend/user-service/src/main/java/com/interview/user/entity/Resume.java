package com.interview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String name;

    private String phone;

    private String email;

    private String education;

    private String school;

    private String major;

    private Integer workYears;

    private String jobPosition;

    private String skills;

    private String experience;

    private String selfIntroduction;

    private String fileUrl;

    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}