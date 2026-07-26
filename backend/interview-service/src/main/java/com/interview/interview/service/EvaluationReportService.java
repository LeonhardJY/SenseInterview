package com.interview.interview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.interview.entity.EvaluationReport;

public interface EvaluationReportService extends IService<EvaluationReport> {
    EvaluationReport findByTaskId(Long taskId);
}