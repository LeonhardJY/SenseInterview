package com.interview.interview.lock;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

/**
 * 基于 Redis 的分布式锁：SET key token NX PX 原子加锁 + Lua 脚本比对 token 原子释放。
 * <p>
 * 加锁用 setIfAbsent(SET NX) + TTL 一条命令原子完成，TTL 防止持锁者崩溃导致死锁；
 * 释放用 Lua“get 比对 token 再 del”，避免锁已过期被他人持有时被误删。
 * <p>
 * 这是够用于短临界区（如会话上下文读改写）的轻量实现；若需可重入、看门狗自动续期、
 * 公平锁等能力，生产可替换为 Redisson 的 RLock。
 */
@Component
public class RedisDistributedLock {

    /** 释放锁脚本：仅当 value 等于持有者 token 时才删除，返回 1 表示释放成功 */
    private static final RedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisDistributedLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试加锁（单次 SET NX PX）。
     *
     * @param ttlMs 锁过期时间（毫秒），防止持锁进程崩溃后死锁
     * @return 成功返回唯一持有者 token（释放时校验），失败返回 null
     */
    public String tryLock(String key, long ttlMs) {
        String token = UUID.randomUUID().toString();
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, token, Duration.ofMillis(ttlMs));
        return Boolean.TRUE.equals(acquired) ? token : null;
    }

    /**
     * 释放锁：Lua 原子比对 token 后删除，避免误删他人持有的锁。
     *
     * @return 释放成功返回 true；token 不匹配或锁已不存在返回 false
     */
    public boolean unlock(String key, String token) {
        Long released = redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
        return released != null && released > 0;
    }
}
