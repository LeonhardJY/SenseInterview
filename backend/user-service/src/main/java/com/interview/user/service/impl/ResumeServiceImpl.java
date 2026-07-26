package com.interview.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.user.entity.Resume;
import com.interview.user.mapper.ResumeMapper;
import com.interview.user.service.ResumeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements ResumeService {

    @Override
    public List<Resume> findByUserId(Long userId) {
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resume::getUserId, userId);
        wrapper.orderByDesc(Resume::getCreateTime);
        return list(wrapper);
    }

    @Override
    public Resume createResume(Resume resume) {
        resume.setCreateTime(LocalDateTime.now());
        resume.setUpdateTime(LocalDateTime.now());
        resume.setStatus(1);
        save(resume);
        return resume;
    }

    @Override
    public Resume updateResume(Resume resume) {
        resume.setUpdateTime(LocalDateTime.now());
        updateById(resume);
        return resume;
    }

    @Override
    public void deleteResume(Long id) {
        removeById(id);
    }
}