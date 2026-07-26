package com.interview.interview.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.interview.entity.HotQuestion;
import com.interview.interview.mapper.HotQuestionMapper;
import com.interview.interview.service.HotQuestionService;
import org.springframework.stereotype.Service;

@Service
public class HotQuestionServiceImpl extends ServiceImpl<HotQuestionMapper, HotQuestion> implements HotQuestionService {
}