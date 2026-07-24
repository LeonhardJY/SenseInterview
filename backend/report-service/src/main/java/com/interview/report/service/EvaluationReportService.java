package com.interview.report.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.report.entity.EvaluationReport;

public interface EvaluationReportService extends IService<EvaluationReport> {

    EvaluationReport findByTaskId(Long taskId);
}