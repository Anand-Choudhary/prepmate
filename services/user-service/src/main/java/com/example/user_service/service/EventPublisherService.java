package com.example.user_service.service;

import com.example.user_service.entity.GenericEvent;
import com.example.user_service.util.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService
{
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private NewTopic userRegistered;

    public void publishUserRegistered(String userId, String email, String name,
                                      String phoneNumber) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("email", email);
        payload.put("name", name);
        payload.put("phoneNumber", phoneNumber);

        GenericEvent event = GenericEvent.builder()
                .eventType(EventType.USER_REGISTERED)
                .occurredAt(LocalDateTime.now())
                .payload(payload)
                .metadata(createMetadata("user-service"))
                .build();

        publishEvent(userRegistered.name(), userId, event);
    }

    private CompletableFuture<Void> publishEvent(String topic, String key, GenericEvent event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

        return future.handle((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event to topic {}: {}",
                        topic, event.getEventId(), ex);
            } else {
                log.info("Published event {} to topic {} partition {}",
                        event.getEventId(),
                        topic,
                        result.getRecordMetadata().partition());
            }
            return null;
        });
    }

    private Map<String, Object> createMetadata(String source) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", source);
        metadata.put("version", "1.0");
        metadata.put("publishedAt", LocalDateTime.now());
        return metadata;
    }


}
