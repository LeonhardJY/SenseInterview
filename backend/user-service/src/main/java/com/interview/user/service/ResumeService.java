package com.interview.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.user.entity.Resume;

import java.util.List;

public interface ResumeService extends IService<Resume> {

    List<Resume> findByUserId(Long userId);

    Resume createResume(Resume resume);

    Resume updateResume(Resume resume);

    void deleteResume(Long id);
}