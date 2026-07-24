package com.interview.question.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.question.entity.QuestionBank;
import com.interview.question.mapper.QuestionBankMapper;
import com.interview.question.service.QuestionBankService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {

    @Override
    public List<QuestionBank> findByCategory(String category) {
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBank::getCategory, category);
        return list(wrapper);
    }

    @Override
    public List<QuestionBank> findByLevel(String level) {
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionBank::getLevel, level);
        return list(wrapper);
    }
}