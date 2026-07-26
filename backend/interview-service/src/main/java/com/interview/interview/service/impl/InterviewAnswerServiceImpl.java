package com.interview.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.interview.interview.entity.InterviewAnswer;
import com.interview.interview.mapper.InterviewAnswerMapper;
import com.interview.interview.service.InterviewAnswerService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewAnswerServiceImpl extends ServiceImpl<InterviewAnswerMapper, InterviewAnswer> implements InterviewAnswerService {

    @Override
    public List<InterviewAnswer> findByRecordId(Long recordId) {
        LambdaQueryWrapper<InterviewAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewAnswer::getRecordId, recordId);
        return list(wrapper);
    }

    @Override
    public InterviewAnswer createAnswer(Long recordId, String answerText, String audioUrl, String videoUrl) {
        InterviewAnswer answer = new InterviewAnswer();
        answer.setRecordId(recordId);
        answer.setAnswerText(answerText);
        answer.setAudioUrl(audioUrl);
        answer.setVideoUrl(videoUrl);
        answer.setCreateTime(LocalDateTime.now());
        save(answer);
        return answer;
    }
}