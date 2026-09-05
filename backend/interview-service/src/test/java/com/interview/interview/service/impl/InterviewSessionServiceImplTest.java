package com.interview.interview.service.impl;

import com.interview.interview.lock.RedisDistributedLock;
import com.interview.interview.model.InterviewContext;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 面试会话缓存单元测试：验证 addQaRecord 的读改写被分布式锁保护。
 * <p>
 * 背景：addQaRecord 是 get→改→set 整个会话对象的 read-modify-write，
 * 并发提交回答会互相覆盖丢更新，需在临界区加锁（对应面试 Q10/Q20）。
 */
class InterviewSessionServiceImplTest {

    @SuppressWarnings("unchecked")
    private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
    private final RedisDistributedLock distributedLock = mock(RedisDistributedLock.class);
    private final InterviewSessionServiceImpl service =
            new InterviewSessionServiceImpl(redisTemplate, distributedLock);

    @Test
    void addQaRecord_获取锁后读改写并在finally释放锁() {
        when(distributedLock.tryLock("lock:interview:session:1", 10_000L)).thenReturn("tok");
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        InterviewContext ctx = new InterviewContext();
        ctx.setTaskId(1L);
        when(valueOps.get("interview:session:1")).thenReturn(ctx);

        service.addQaRecord(1L, "Q", "A");

        assertThat(ctx.getHistory()).hasSize(1);
        verify(valueOps).set(eq("interview:session:1"), any(InterviewContext.class), eq(2L), eq(TimeUnit.HOURS));
        verify(distributedLock).unlock("lock:interview:session:1", "tok");
    }

    @Test
    void addQaRecord_未抢到锁时跳过读改写避免并发覆盖() {
        when(distributedLock.tryLock(anyString(), anyLong())).thenReturn(null);

        service.addQaRecord(1L, "Q", "A");

        verify(redisTemplate, never()).opsForValue();
        verify(distributedLock, never()).unlock(anyString(), anyString());
    }

    @Test
    void addQaRecord_会话不存在时不写缓存但仍在finally释放锁() {
        when(distributedLock.tryLock(anyString(), anyLong())).thenReturn("tok");
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("interview:session:1")).thenReturn(null);

        service.addQaRecord(1L, "Q", "A");

        verify(valueOps, never()).set(anyString(), any(), anyLong(), any(TimeUnit.class));
        verify(distributedLock).unlock(anyString(), eq("tok"));
    }
}
