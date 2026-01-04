package com.interview_platform.interview_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@Slf4j
@RequiredArgsConstructor
public class DistributedLockService {

    private final RedissonClient redissonClient;

    @Value("${interview.lock.wait-time-seconds:5}")
    private long waitTime;

    @Value("${interview.lock.lease-time-seconds:30}")
    private long leaseTime;

    private static final String LOCK_PREFIX = "lock:slot:";

    /**
     * Execute operation with distributed lock
     */
    public <T> T executeWithLock(String slotId, Supplier<T> operation) {
        String lockKey = LOCK_PREFIX + slotId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Try to acquire lock with timeout
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);

            if (!acquired) {
                log.warn("Failed to acquire lock for slot: {}", slotId);
                throw new LockAcquisitionException("Could not acquire lock for slot: " + slotId);
            }

            log.debug("Lock acquired for slot: {}", slotId);

            // Execute the operation
            return operation.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Lock acquisition interrupted for slot: {}", slotId, e);
            throw new LockAcquisitionException("Lock acquisition interrupted", e);
        } finally {
            // Release lock if held by current thread
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Lock released for slot: {}", slotId);
            }
        }
    }

    /**
     * Execute void operation with distributed lock
     */
    public void executeWithLock(String slotId, Runnable operation) {
        executeWithLock(slotId, () -> {
            operation.run();
            return null;
        });
    }

    /**
     * Check if lock exists for a slot
     */
    public boolean isLocked(String slotId) {
        String lockKey = LOCK_PREFIX + slotId;
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isLocked();
    }

    /**
     * Force unlock (use carefully)
     */
    public void forceUnlock(String slotId) {
        String lockKey = LOCK_PREFIX + slotId;
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isLocked()) {
            lock.forceUnlock();
            log.warn("Forced unlock for slot: {}", slotId);
        }
    }

    public static class LockAcquisitionException extends RuntimeException {
        public LockAcquisitionException(String message) {
            super(message);
        }

        public LockAcquisitionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
