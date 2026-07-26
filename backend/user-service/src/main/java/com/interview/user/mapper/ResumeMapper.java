package com.interview.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.user.entity.Resume;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ResumeMapper extends BaseMapper<Resume> {
}