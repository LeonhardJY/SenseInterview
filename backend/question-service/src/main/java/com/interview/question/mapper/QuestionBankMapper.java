package com.interview.question.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.interview.question.entity.QuestionBank;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {
}