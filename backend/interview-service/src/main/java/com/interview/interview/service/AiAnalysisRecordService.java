package com.interview.interview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.interview.entity.AiAnalysisRecord;

import java.util.List;

public interface AiAnalysisRecordService extends IService<AiAnalysisRecord> {
    List<AiAnalysisRecord> findByTaskId(Long taskId);
}