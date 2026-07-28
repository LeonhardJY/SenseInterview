package com.interview.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.user.entity.Resume;
import com.interview.user.mapper.ResumeMapper;
import com.interview.user.service.ResumeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Resume findDefaultByUserId(Long userId) {
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resume::getUserId, userId);
        wrapper.eq(Resume::getStatus, 2); // status=2 表示默认简历
        return getOne(wrapper);
    }

    @Override
    public Resume createResume(Resume resume) {
        resume.setCreateTime(LocalDateTime.now());
        resume.setUpdateTime(LocalDateTime.now());
        // 如果是用户第一份简历，设为默认
        List<Resume> existing = findByUserId(resume.getUserId());
        if (existing.isEmpty()) {
            resume.setStatus(2); // 默认简历
        } else {
            resume.setStatus(1); // 普通简历
        }
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
    @Transactional
    public void setDefaultResume(Long id) {
        Resume resume = getById(id);
        if (resume == null) return;

        // 取消该用户其他默认简历
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resume::getUserId, resume.getUserId());
        wrapper.eq(Resume::getStatus, 2);
        List<Resume> defaults = list(wrapper);
        for (Resume r : defaults) {
            r.setStatus(1);
            updateById(r);
        }

        // 设置当前简历为默认
        resume.setStatus(2);
        updateById(resume);
    }

    @Override
    public void deleteResume(Long id) {
        removeById(id);
    }
}