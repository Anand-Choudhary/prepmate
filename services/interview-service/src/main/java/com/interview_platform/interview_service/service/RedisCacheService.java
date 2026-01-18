package com.interview_platform.interview_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview_platform.interview_service.dto.SlotResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String AVAILABLE_SLOTS_PREFIX = "cache:slots:available:";
    private static final String BOOKING_ATTEMPT_PREFIX = "booking:attempt:";
    private static final Duration SLOTS_CACHE_TTL = Duration.ofMinutes(2);
    private static final Duration BOOKING_ATTEMPT_TTL = Duration.ofMinutes(5);

    // Cache available slots for an interviewer
    public void cacheAvailableSlots(Long interviewerId, List<SlotResponse> slots) {
        try {
            String key = AVAILABLE_SLOTS_PREFIX + interviewerId;
            String json = objectMapper.writeValueAsString(slots);
            redisTemplate.opsForValue().set(key, json, SLOTS_CACHE_TTL);
            log.debug("Cached {} available slots for interviewer: {}", slots.size(), interviewerId);
        } catch (Exception e) {
            log.error("Error caching available slots", e);
        }
    }

    // Get cached available slots
    public List<SlotResponse> getCachedAvailableSlots(Long interviewerId) {
        try {
            String key = AVAILABLE_SLOTS_PREFIX + interviewerId;
            String json = (String) redisTemplate.opsForValue().get(key);

            if (json != null) {
                log.debug("Cache hit for available slots: {}", interviewerId);
                return objectMapper.readValue(json, new TypeReference<List<SlotResponse>>() {});
            }

            log.debug("Cache miss for available slots: {}", interviewerId);
            return null;
        } catch (Exception e) {
            log.error("Error reading cached slots", e);
            return null;
        }
    }

    // Invalidate slots cache
    public void invalidateSlotsCache(Long interviewerId) {
        try {
            String key = AVAILABLE_SLOTS_PREFIX + interviewerId;
            redisTemplate.delete(key);
            log.debug("Invalidated slots cache for interviewer: {}", interviewerId);
        } catch (Exception e) {
            log.error("Error invalidating slots cache", e);
        }
    }

    // Track booking attempts (prevent spam/double booking)
    public boolean hasRecentBookingAttempt(Long userId, Long slotId) {
        try {
            String key = BOOKING_ATTEMPT_PREFIX + userId + ":" + slotId;
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis failure while checking booking attempt", e);
            throw new IllegalStateException("Booking system temporarily unavailable. Please try again.");
        }
    }

    // Record booking attempt
    public void recordBookingAttempt(Long userId, Long slotId) {
        try {
            String key = BOOKING_ATTEMPT_PREFIX + userId + ":" + slotId;
            redisTemplate.opsForValue().set(key, "1", BOOKING_ATTEMPT_TTL);
            log.debug("Recorded booking attempt for user: {}, slot: {}", userId, slotId);
        } catch (Exception e)
        {
            log.error("Error recording booking attempt", e);
            throw new IllegalStateException("Booking system temporarily unavailable. Please try again.");
        }
    }

    // Clear booking attempt
    public void clearBookingAttempt(Long userId, Long slotId) {
        try {
            String key = BOOKING_ATTEMPT_PREFIX + userId + ":" + slotId;
            redisTemplate.delete(key);
            log.debug("Cleared booking attempt for user: {}, slot: {}", userId, slotId);
        } catch (Exception e)
        {
            log.error("Error clearing booking attempt", e);
            throw new IllegalStateException("Booking system temporarily unavailable. Please try again.");
        }
    }

    // Generic cache operations
    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.error("Error setting cache value", e);
        }
    }

    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Error getting cache value", e);
            return null;
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error deleting cache value", e);
        }
    }
}