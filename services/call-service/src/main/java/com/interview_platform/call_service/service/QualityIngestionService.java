package com.interview_platform.call_service.service;

import com.interview_platform.call_service.dto.MinuteQualityDto;
import com.interview_platform.call_service.entity.MinuteQuality;
import com.interview_platform.call_service.repository.MinuteQualityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.concurrent.CompletedFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class QualityIngestionService {

    private final KafkaTemplate<String, MinuteQualityDto> callQualityKafkaTemplate;
    private final MinuteQualityRepository minuteQualityRepository;
    private final QualityCacheService qualityCacheService;

    @Value("${app.kafka.send-timeout-ms:2000}")
    private long kafkaSendTimeoutMs;


    @KafkaListener(
            topics = "call-quality-data",
            groupId = "quality-cache-group",
            containerFactory = "callQualityListenerContainerFactory"
    )
    public void consumeQualityData(MinuteQualityDto qualityData) {
        try {
            log.debug("Consumed quality data for room: {} at {}",
                    qualityData.getRoomToken(),
                    qualityData.getMinuteNumber());

            qualityCacheService.storeQualityData(qualityData);

        } catch (Exception e) {
            log.error("Failed to process quality data for room: {}",
                    qualityData.getRoomToken(), e);
            throw e;
        }
    }


    public void ingestMinuteQuality(MinuteQualityDto qualityDto) {
        String key = buildKafkaKey(qualityDto);

        log.info("📤 Attempting to send minute quality: room={}, user={}, minute={}",
                qualityDto.getRoomToken(),
                qualityDto.getUserId(),
                qualityDto.getMinuteNumber());

        try {
            boolean kafkaSuccess = sendToKafka(key, qualityDto);
            if(!kafkaSuccess)
            {
                log.warn("Kafka unavailable for room: {}. Storing in fallback DB",
                        qualityDto.getRoomToken());
                saveToFallbackDb(qualityDto);
            }

        } catch (Exception e) {
            log.error("Kafka send failed for room={}, user={}, minute={} - Using fallback DB: {}",
                    qualityDto.getRoomToken(),
                    qualityDto.getUserId(),
                    qualityDto.getMinuteNumber(),
                    e.getMessage());
            try{
                saveToFallbackDb(qualityDto);
            }
            catch (Exception dbError)
            {
                log.error("CRITICAL: Failed to store quality data in both Kafka AND DB for room: {}",
                        qualityDto.getRoomToken(), dbError);
            }

        }
    }

    private boolean sendToKafka(String key, MinuteQualityDto qualityDto) {
        try {

            CompletableFuture<SendResult<String, MinuteQualityDto>> future=
                    callQualityKafkaTemplate.send(
                            "call-quality-data",
                            key,
                            qualityDto
                    );

            SendResult<String, MinuteQualityDto> result = future.get(kafkaSendTimeoutMs, TimeUnit.MILLISECONDS);


            log.debug("Sent to Kafka: topic={}, partition={}, offset={}, room={}, user={}, minute={}",
                    "call-quality-data",
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset(),
                    qualityDto.getRoomToken(),
                    qualityDto.getUserId(),
                    qualityDto.getMinuteNumber());

            return true;
        } catch (java.util.concurrent.TimeoutException e)
        {
            log.error("Kafka send timeout for room: {} - Kafka is slow or down",
                    qualityDto.getRoomToken());
            return false;
        } catch (Exception e) {
            log.error("Failed to send quality data to Kafka for room: {}: {}",
                    qualityDto.getRoomToken(), e.getMessage());
            return false;
        }
    }

    /**
     * Save to fallback database when Kafka is unavailable
     */
    @Transactional
    private void saveToFallbackDb(MinuteQualityDto qualityDto) {
        try {
            MinuteQuality fallback = MinuteQuality.builder()
                    .roomToken(qualityDto.getRoomToken())
                    .userId(qualityDto.getUserId())
                    .minuteNumber(qualityDto.getMinuteNumber())
                    .build();

            minuteQualityRepository.save(fallback);

            log.info("💾 Saved to fallback DB: room={}, user={}, minute={}",
                    qualityDto.getRoomToken(),
                    qualityDto.getUserId(),
                    qualityDto.getMinuteNumber()
                    );

        } catch (Exception e) {
            log.error("🚨 CRITICAL: Failed to save to fallback DB! Data may be lost! room={}, user={}, minute={}",
                    qualityDto.getRoomToken(),
                    qualityDto.getUserId(),
                    qualityDto.getMinuteNumber(),
                    e);
            throw new RuntimeException("Fallback DB save failed - data loss risk!", e);
        }
    }

    /**
     * Build Kafka partition key for ordering guarantee
     * Same user's messages go to same partition in order
     */
    private String buildKafkaKey(MinuteQualityDto qualityDto) {
        return qualityDto.getRoomToken() + ":" + qualityDto.getUserId();
    }

    /**
     * Custom exception for Kafka timeout
     */

}
