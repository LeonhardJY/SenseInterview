package com.interview.interview.lock;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis 分布式锁单元测试：验证 SET NX PX 加锁与 Lua 比对 token 释放的核心语义。
 */
class RedisDistributedLockTest {

    @SuppressWarnings("unchecked")
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> ops = mock(ValueOperations.class);
    private final RedisDistributedLock lock = new RedisDistributedLock(redis);

    @Test
    void tryLock_SET_NX_PX成功时返回唯一token并按TTL写入() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(eq("lock:k"), anyString(), eq(Duration.ofMillis(10000L)))).thenReturn(true);

        String token = lock.tryLock("lock:k", 10000L);

        assertThat(token).isNotNull();
        // 写入的 value 必须与返回的 token 一致（释放时据此比对持有者）
        verify(ops).setIfAbsent(eq("lock:k"), eq(token), eq(Duration.ofMillis(10000L)));
    }

    @Test
    void tryLock_键已存在时返回null表示未抢到锁() {
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        assertThat(lock.tryLock("lock:k", 10000L)).isNull();
    }

    @Test
    void unlock_Lua比对token一致时删除并返回true() {
        when(redis.execute(any(RedisScript.class), anyList(), eq("tok"))).thenReturn(1L);

        assertThat(lock.unlock("lock:k", "tok")).isTrue();
    }

    @Test
    void unlock_token不匹配时不删除返回false() {
        when(redis.execute(any(RedisScript.class), anyList(), eq("stale"))).thenReturn(0L);

        assertThat(lock.unlock("lock:k", "stale")).isFalse();
    }
}
