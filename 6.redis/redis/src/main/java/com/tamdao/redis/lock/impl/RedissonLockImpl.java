package com.tamdao.redis.lock.impl;

import com.tamdao.redis.lock.DistributedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedissonLockImpl implements DistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedissonLockImpl.class);
    private final RedissonClient redissonClient;

    public RedissonLockImpl(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean acquire(String lockKey, String lockValue, long expireTimeInMs) {
        try {
            RLock lock = redissonClient.getLock(lockKey);
            // Thử lấy khóa với waitTime = 0 (lấy ngay lập tức, không chờ) và leaseTime = expireTimeInMs
            boolean acquired = lock.tryLock(0, expireTimeInMs, TimeUnit.MILLISECONDS);
            if (acquired) {
                log.info("Successfully acquired Redisson Lock. [Key: {}, LeaseTime: {}ms]", lockKey, expireTimeInMs);
            } else {
                log.warn("Failed to acquire Redisson Lock. [Key: {}]", lockKey);
            }
            return acquired;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted while acquiring Redisson Lock for key: " + lockKey, e);
            return false;
        } catch (Exception e) {
            log.error("Error acquiring Redisson Lock for key: " + lockKey, e);
            return false;
        }
    }

    @Override
    public boolean release(String lockKey, String lockValue) {
        try {
            RLock lock = redissonClient.getLock(lockKey);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Successfully released Redisson Lock. [Key: {}]", lockKey);
                return true;
            } else {
                log.warn("Failed to release Redisson Lock. Lock is not held by the current thread. [Key: {}]", lockKey);
                return false;
            }
        } catch (Exception e) {
            log.error("Error releasing Redisson Lock for key: " + lockKey, e);
            return false;
        }
    }
}
