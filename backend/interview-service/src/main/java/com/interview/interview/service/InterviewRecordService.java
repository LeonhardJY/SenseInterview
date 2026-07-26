package com.interview.interview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.interview.entity.InterviewRecord;

import java.util.List;

public interface InterviewRecordService extends IService<InterviewRecord> {

    List<InterviewRecord> findByTaskId(Long taskId);

    InterviewRecord createRecord(Long taskId, Integer roundNum, String question);
}