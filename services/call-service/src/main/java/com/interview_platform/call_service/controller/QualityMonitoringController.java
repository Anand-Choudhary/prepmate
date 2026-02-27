package com.interview_platform.call_service.controller;

import com.interview_platform.call_service.config.KafkaTopicConfig;
import com.interview_platform.call_service.dto.MinuteQualityDto;
import com.interview_platform.call_service.service.QualityIngestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Controller
@RequiredArgsConstructor
@Slf4j
public class QualityMonitoringController {

//    private final QualityCacheService qualityCacheService;
    private final KafkaTemplate<String,MinuteQualityDto> callQualityKafkaTemplate;
    private final QualityIngestionService qualityIngestionService;
//    private final SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/quality/minute")
    public void handleMinuteQuality(@Valid @Payload MinuteQualityDto qualityDto)
    {
        log.info("📤Attempting to send minute quality: room={}, user={}, minute={}, quality={}, minuteStart={}, minuteEnd={}",
                qualityDto.getRoomToken(),
                qualityDto.getUserId(),
                qualityDto.getMinuteNumber(),
                qualityDto.getIsGoodQuality(),
                qualityDto.getMinuteStartTime(),
                qualityDto.getMinuteEndTime()
        );

        try {
            if (qualityDto.getRoomToken() == null || qualityDto.getRoomToken().isEmpty()) {
                log.error("Invalid quality data: missing roomId");
                return;
            }

            log.debug("Received quality data for room: {} at {}",
                    qualityDto.getRoomToken(),
                    qualityDto.getMinuteNumber());

            qualityIngestionService.ingestMinuteQuality(qualityDto);
        } catch (Exception e) {
            log.error("Critical error handling quality data for room: {}",
                    qualityDto.getRoomToken(), e);

        }
    }


    private void sendAlert(String roomId, String message) {
        // TODO: Implement alerting (Slack, PagerDuty, email, etc.)
        log.error("🚨 ALERT: {} for room: {}", message, roomId);
    }
}