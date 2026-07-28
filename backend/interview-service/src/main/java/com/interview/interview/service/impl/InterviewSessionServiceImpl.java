package com.interview.interview.service.impl;

import com.interview.interview.model.InterviewContext;
import com.interview.interview.service.InterviewSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的面试会话缓存实现
 * <p>
 * Key 格式：interview:session:{taskId}
 * 过期时间：2 小时（一场面试通常不会超过 2 小时）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSessionServiceImpl implements InterviewSessionService {

    private static final String SESSION_KEY_PREFIX = "interview:session:";
    private static final long SESSION_EXPIRE_HOURS = 2;

    private final RedisTemplate<String, Object> redisTemplate;

    private String buildKey(Long taskId) {
        return SESSION_KEY_PREFIX + taskId;
    }

    @Override
    public void initSession(Long taskId, Long userId, String jobName, String mode, String difficulty) {
        InterviewContext context = new InterviewContext();
        context.setTaskId(taskId);
        context.setUserId(userId);
        context.setJobName(jobName);
        context.setMode(mode);
        context.setDifficulty(difficulty);
        context.setStatus("CREATED");
        context.setLastAccessTime(LocalDateTime.now());

        String key = buildKey(taskId);
        redisTemplate.opsForValue().set(key, context, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);
        log.info("面试会话已初始化 — taskId: {}, userId: {}, job: {}", taskId, userId, jobName);
    }

    @Override
    public InterviewContext getContext(Long taskId) {
        String key = buildKey(taskId);
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj instanceof InterviewContext) {
            return (InterviewContext) obj;
        }
        return null;
    }

    @Override
    public void setContext(Long taskId, InterviewContext context) {
        if (context == null) return;
        context.setLastAccessTime(LocalDateTime.now());

        String key = buildKey(taskId);
        redisTemplate.opsForValue().set(key, context, SESSION_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    @Override
    public void addQaRecord(Long taskId, String question, String answer) {
        InterviewContext context = getContext(taskId);
        if (context == null) {
            log.warn("面试会话不存在，无法添加问答记录 — taskId: {}", taskId);
            return;
        }
        context.addQa(question, answer);
        setContext(taskId, context);
        log.debug("问答记录已添加 — taskId: {}, round: {}", taskId, context.getCurrentRound());
    }

    @Override
    public void addEmotionRecord(Long taskId, String emotion, Double confidence) {
        InterviewContext context = getContext(taskId);
        if (context == null) {
            log.warn("面试会话不存在，无法添加情绪记录 — taskId: {}", taskId);
            return;
        }
        context.addEmotion(emotion, confidence);
        setContext(taskId, context);
        log.debug("情绪记录已添加 — taskId: {}, emotion: {}, confidence: {}", taskId, emotion, confidence);
    }

    @Override
    public void updateStatus(Long taskId, String status) {
        InterviewContext context = getContext(taskId);
        if (context == null) {
            log.warn("面试会话不存在，无法更新状态 — taskId: {}", taskId);
            return;
        }
        context.setStatus(status);
        if ("RUNNING".equals(status)) {
            context.setStartTime(LocalDateTime.now());
        } else if ("FINISHED".equals(status)) {
            context.setEndTime(LocalDateTime.now());
        }
        setContext(taskId, context);
        log.info("面试状态已更新 — taskId: {}, status: {}", taskId, status);
    }

    @Override
    public Integer getCurrentRound(Long taskId) {
        InterviewContext context = getContext(taskId);
        return context != null ? context.getCurrentRound() : 0;
    }

    @Override
    public String getHistoryText(Long taskId) {
        InterviewContext context = getContext(taskId);
        if (context == null) {
            return "";
        }
        return context.buildHistoryText();
    }

    @Override
    public void removeSession(Long taskId) {
        String key = buildKey(taskId);
        redisTemplate.delete(key);
        log.info("面试会话已清除 — taskId: {}", taskId);
    }

    @Override
    public boolean hasSession(Long taskId) {
        String key = buildKey(taskId);
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }
}
