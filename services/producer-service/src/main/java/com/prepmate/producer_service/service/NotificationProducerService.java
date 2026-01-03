package com.prepmate.producer_service.service;



import com.fasterxml.jackson.databind.ObjectMapper;
import com.prepmate.producer_service.dao.NotificationRepository;
import com.prepmate.producer_service.dto.*;
import com.prepmate.producer_service.entities.NotificationEntity;
import com.prepmate.producer_service.utils.NotificationStatus;
import com.prepmate.producer_service.utils.NotificationType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducerService
{
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationRepository notificationRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.email}")
    private String emailTopic;

    @Value("${kafka.topics.sms}")
    private String smsTopic;

    @Value("${kafka.topics.push}")
    private String pushTopic;

    @Value("${kafka.topics.webhook}")
    private String webhookTopic;

    private static final String DEDUP_KEY_PREFIX = "notification:dedup:";
    private static final long DEDUP_TTL_SECONDS = 300;

    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendNotificationFallback")
    @RateLimiter(name = "notificationService")
    @Transactional
    public CompletableFuture<NotificationResponse> sendNotification(NotificationRequest request) {

        // Deduplication check
        if (isDuplicate(request.getId())) {
            log.warn("Duplicate notification detected: {}", request.getId());
            return CompletableFuture.completedFuture(
                    new NotificationResponse(request.getId(), NotificationStatus.FAILED,
                            "Duplicate notification", LocalDateTime.now())
            );
        }

        // Save to database
        NotificationEntity entity = saveNotificationEntity(request);

        // Determine topic based on type
        String topic = getTopicByType(request.getType());

        // Send to Kafka asynchronously
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, request.getUserId(), request);

        return future.handle((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send notification to Kafka: {}", request.getId(), ex);
                updateNotificationStatus(entity.getId(), NotificationStatus.FAILED, ex.getMessage());
                return new NotificationResponse(request.getId(), NotificationStatus.FAILED,
                        ex.getMessage(), LocalDateTime.now());
            } else {
                log.info("Notification sent to Kafka successfully: {} to topic: {}",
                        request.getId(), topic);
                updateNotificationStatus(entity.getId(), NotificationStatus.PROCESSING, null);
                markAsProcessed(request.getId());
                return new NotificationResponse(request.getId(), NotificationStatus.PROCESSING,
                        "Notification queued successfully", LocalDateTime.now());
            }
        });
    }

    @Transactional
    public List<CompletableFuture<NotificationResponse>> sendBatchNotifications(
            List<NotificationRequest> requests) {

        log.info("Processing batch of {} notifications", requests.size());

        return requests.stream()
                .map(this::sendNotification)
                .collect(Collectors.toList());
    }

    private NotificationEntity saveNotificationEntity(NotificationRequest request) {
        NotificationEntity entity = NotificationEntity.builder()
                .userId(request.getUserId())
                .type(request.getType())
                .status(NotificationStatus.PENDING)
                .priority(request.getPriority())
                .recipient(extractRecipient(request))
                .content(extractContent(request))
                .templateId(request.getTemplateId())
                .scheduledAt(request.getScheduledAt())
                .retryCount(0)
                .metadata(serializeMetadata(request.getMetadata()))
                .build();

        return notificationRepository.save(entity);
    }

    private void updateNotificationStatus(String id, NotificationStatus status, String errorMessage) {
        notificationRepository.findById(id).ifPresent(entity -> {
            entity.setStatus(status);
            entity.setErrorMessage(errorMessage);
            if (status == NotificationStatus.SENT) {
                entity.setSentAt(LocalDateTime.now());
            }
            notificationRepository.save(entity);
        });
    }

    private boolean isDuplicate(String notificationId) {
        String key = DEDUP_KEY_PREFIX + notificationId;
        return redisTemplate.hasKey(key);
    }

    private void markAsProcessed(String notificationId) {
        String key = DEDUP_KEY_PREFIX + notificationId;
        redisTemplate.opsForValue().set(key, "1", DEDUP_TTL_SECONDS, TimeUnit.SECONDS);
    }

    private String getTopicByType(NotificationType type) {
        return switch (type) {
            case EMAIL -> emailTopic;
            case SMS -> smsTopic;
            case PUSH -> pushTopic;
            case WEBHOOK -> webhookTopic;
        };
    }

    private String extractRecipient(NotificationRequest request) {

        if (request instanceof EmailNotificationRequest email) {
            return email.getTo();
        } else if (request instanceof SmsNotificationRequest sms) {
            return sms.getPhoneNumber();
        } else if (request instanceof PushNotificationRequest push) {
            return push.getDeviceToken();
        } else if (request instanceof WebhookNotificationRequest webhook) {
            return webhook.getUrl();
        }
        return "unknown";
    }


    private String extractContent(NotificationRequest request) {

        if (request instanceof EmailNotificationRequest email) {
            return email.getSubject();
        } else if (request instanceof SmsNotificationRequest sms) {
            return sms.getMessage();
        } else if (request instanceof PushNotificationRequest push) {
            return push.getTitle();
        } else if (request instanceof WebhookNotificationRequest webhook) {
            return webhook.getMethod();
        }
        return "";
    }

    private String serializeMetadata(Object metadata) {
        try {
            return metadata != null ? objectMapper.writeValueAsString(metadata) : null;
        } catch (Exception e) {
            log.error("Failed to serialize metadata", e);
            return null;
        }
    }

    // Fallback method for circuit breaker
    public CompletableFuture<NotificationResponse> sendNotificationFallback(
            NotificationRequest request, Exception ex) {

        log.error("Circuit breaker activated for notification: {}", request.getId(), ex);

        return CompletableFuture.completedFuture(
                new NotificationResponse(request.getId(), NotificationStatus.FAILED,
                        "Service temporarily unavailable", LocalDateTime.now())
        );
    }
}
