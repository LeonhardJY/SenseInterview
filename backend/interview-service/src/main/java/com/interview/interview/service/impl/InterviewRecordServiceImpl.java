package com.interview.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.interview.entity.InterviewRecord;
import com.interview.interview.mapper.InterviewRecordMapper;
import com.interview.interview.service.InterviewRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewRecordServiceImpl extends ServiceImpl<InterviewRecordMapper, InterviewRecord> implements InterviewRecordService {

    @Override
    public List<InterviewRecord> findByTaskId(Long taskId) {
        LambdaQueryWrapper<InterviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewRecord::getTaskId, taskId);
        wrapper.orderByAsc(InterviewRecord::getRoundNum);
        return list(wrapper);
    }

    @Override
    public InterviewRecord createRecord(Long taskId, Integer roundNum, String question) {
        InterviewRecord record = new InterviewRecord();
        record.setTaskId(taskId);
        record.setRoundNum(roundNum);
        record.setQuestion(question);
        record.setCreateTime(LocalDateTime.now());
        save(record);
        return record;
    }
}