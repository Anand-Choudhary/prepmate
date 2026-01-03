package com.prepmate.consumer_service.consumer;


import com.prepmate.consumer_service.entity.GenericEvent;
import com.prepmate.consumer_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.topics.user-registered}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeUserRegistered(
            @Payload GenericEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            Acknowledgment acknowledgment) {

        log.info("Received USER_REGISTERED event: {} from partition: {}",
                event.getEventId(), partition);

        try {
            notificationService.handleEvent(event);
            acknowledgment.acknowledge();
            log.info("Successfully processed USER_REGISTERED event: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Failed to process USER_REGISTERED event: {}", event.getEventId(), e);
            acknowledgment.acknowledge();
        }
    }

}
