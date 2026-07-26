package com.interview.interview.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.interview.entity.JobPosition;
import com.interview.interview.mapper.JobPositionMapper;
import com.interview.interview.service.JobPositionService;
import org.springframework.stereotype.Service;

@Service
public class JobPositionServiceImpl extends ServiceImpl<JobPositionMapper, JobPosition> implements JobPositionService {
}