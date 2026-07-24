package com.interview.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.report.entity.EvaluationReport;
import com.interview.report.mapper.EvaluationReportMapper;
import com.interview.report.service.EvaluationReportService;
import org.springframework.stereotype.Service;

@Service
public class EvaluationReportServiceImpl extends ServiceImpl<EvaluationReportMapper, EvaluationReport> implements EvaluationReportService {

    @Override
    public EvaluationReport findByTaskId(Long taskId) {
        LambdaQueryWrapper<EvaluationReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvaluationReport::getTaskId, taskId);
        return getOne(wrapper);
    }
}