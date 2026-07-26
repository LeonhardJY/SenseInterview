package com.interview.interview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.interview.interview.entity.InterviewAnswer;

import java.util.List;

public interface InterviewAnswerService extends IService<InterviewAnswer> {

    List<InterviewAnswer> findByRecordId(Long recordId);

    InterviewAnswer createAnswer(Long recordId, String answerText, String audioUrl, String videoUrl);
}