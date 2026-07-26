package com.interview.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.interview.entity.AiAnalysisRecord;
import com.interview.interview.mapper.AiAnalysisRecordMapper;
import com.interview.interview.service.AiAnalysisRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiAnalysisRecordServiceImpl extends ServiceImpl<AiAnalysisRecordMapper, AiAnalysisRecord> implements AiAnalysisRecordService {

    @Override
    public List<AiAnalysisRecord> findByTaskId(Long taskId) {
        LambdaQueryWrapper<AiAnalysisRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAnalysisRecord::getTaskId, taskId);
        return list(wrapper);
    }
}