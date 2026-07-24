package com.interview.evaluation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.evaluation.entity.AiAnalysisRecord;

import java.util.List;

public interface AiAnalysisRecordService extends IService<AiAnalysisRecord> {

    List<AiAnalysisRecord> findByTaskId(Long taskId);
}