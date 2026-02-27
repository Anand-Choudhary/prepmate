package com.interview_platform.call_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview_platform.call_service.dto.MinuteQualityDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class QualityCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis key prefixes
    private static final String QUALITY_POSTFIX = ":quality";
    private static final String ROOM_DATA_PREFIX = "room_data:";
    private static final long CACHE_EXPIRY_HOURS = 2;

    public void initializeRoom(String roomToken)
    {

        String roomKey = ROOM_DATA_PREFIX + roomToken;

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("roomToken", roomToken);
        roomData.put("createdAt", System.currentTimeMillis());
        roomData.put("status", "active");

        redisTemplate.opsForValue().set(roomKey, toJson(roomData));
        redisTemplate.expire(roomKey, CACHE_EXPIRY_HOURS, TimeUnit.HOURS);

        log.info("Initialized Redis room: {}", roomToken);

    }

    public void storeQualityData(MinuteQualityDto qualityData) {
        String roomKey = ROOM_DATA_PREFIX + qualityData.getRoomToken();
        String qualityKey = roomKey + QUALITY_POSTFIX;

        if (!redisTemplate.hasKey(roomKey)) {
            initializeRoom(qualityData.getRoomToken());
        }

        redisTemplate.opsForList().rightPush(qualityKey, toJson(qualityData));
        redisTemplate.expire(qualityKey, CACHE_EXPIRY_HOURS, TimeUnit.HOURS);

        log.info("Stored quality data for room: {}", qualityData.getRoomToken());
    }



    public Map<String, Object> getRoomData(String roomToken) {
        String roomKey = ROOM_DATA_PREFIX + roomToken;
        String qualityKey = roomKey + QUALITY_POSTFIX;

        String roomMetaJson = (String) redisTemplate.opsForValue().get(roomKey);
        if (roomMetaJson == null) {
            log.warn("No room found in Redis for token: {}", roomToken);
            return null;
        }

        List<Object> rawList = redisTemplate.opsForList().range(qualityKey, 0, -1);
        assert rawList != null;
        List<String> qualityJsonList = rawList.stream()
                .map(Object::toString)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("roomMeta", fromJson(roomMetaJson, Map.class));
        result.put("qualityData", qualityJsonList);

        log.info("Fetched room data for token: {}", roomToken);
        return result;
    }


    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting to JSON", e);
            return "{}";
        }
    }

    private <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON: {}", json, e);
            throw new RuntimeException("Failed to deserialize JSON", e);
        }
    }

}