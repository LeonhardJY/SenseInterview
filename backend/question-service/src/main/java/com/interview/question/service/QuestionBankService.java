package com.interview.question.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.question.entity.QuestionBank;

import java.util.List;

public interface QuestionBankService extends IService<QuestionBank> {

    List<QuestionBank> findByCategory(String category);

    List<QuestionBank> findByLevel(String level);
}