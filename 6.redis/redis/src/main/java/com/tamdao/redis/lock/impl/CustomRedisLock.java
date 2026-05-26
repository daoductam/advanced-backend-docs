package com.tamdao.redis.lock.impl;

import com.tamdao.redis.lock.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;

@Component
public class CustomRedisLock implements DistributedLock {

    private static final Logger log = LoggerFactory.getLogger(CustomRedisLock.class);
    private final StringRedisTemplate redisTemplate;

    // Lua Script dùng để giải phóng khóa một cách an toàn (chỉ xóa nếu giá trị khớp với lockValue của luồng hiện tại)
    private static final String RELEASE_SCRIPT = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    private final DefaultRedisScript<Long> releaseRedisScript;

    public CustomRedisLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.releaseRedisScript = new DefaultRedisScript<>();
        this.releaseRedisScript.setScriptText(RELEASE_SCRIPT);
        this.releaseRedisScript.setResultType(Long.class);
    }

    @Override
    public boolean acquire(String lockKey, String lockValue, long expireTimeInMs) {
        try {
            // SET lockKey lockValue NX PX expireTimeInMs
            Boolean success = redisTemplate.opsForValue().setIfAbsent(
                    lockKey, 
                    lockValue, 
                    Duration.ofMillis(expireTimeInMs)
            );
            boolean acquired = Boolean.TRUE.equals(success);
            if (acquired) {
                log.info("Successfully acquired Custom Lock. [Key: {}, Value: {}, TTL: {}ms]", lockKey, lockValue, expireTimeInMs);
            } else {
                log.warn("Failed to acquire Custom Lock. [Key: {}, Value: {}]", lockKey, lockValue);
            }
            return acquired;
        } catch (Exception e) {
            log.error("Error acquiring Custom Lock for key: " + lockKey, e);
            return false;
        }
    }

    @Override
    public boolean release(String lockKey, String lockValue) {
        try {
            // Thực thi Lua Script để đảm bảo tính nguyên tử
            Long result = redisTemplate.execute(
                    releaseRedisScript,
                    Collections.singletonList(lockKey),
                    lockValue
            );
            boolean released = result != null && result > 0;
            if (released) {
                log.info("Successfully released Custom Lock. [Key: {}, Value: {}]", lockKey, lockValue);
            } else {
                log.warn("Failed to release Custom Lock. Key not found or value mismatch. [Key: {}, Value: {}]", lockKey, lockValue);
            }
            return released;
        } catch (Exception e) {
            log.error("Error releasing Custom Lock for key: " + lockKey, e);
            return false;
        }
    }
}
