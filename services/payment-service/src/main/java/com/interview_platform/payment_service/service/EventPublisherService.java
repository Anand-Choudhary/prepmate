package com.interview_platform.payment_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
class EventPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishWalletCreated(String userId) {
        Map<String, Object> event = Map.of(
                "eventType", "WALLET_CREATED",
                "userId", userId,
                "timestamp", LocalDateTime.now()
        );
        kafkaTemplate.send("wallet.created", userId, event);
    }

    public void publishMoneyAdded(String userId, BigDecimal amount) {
        Map<String, Object> event = Map.of(
                "eventType", "MONEY_ADDED",
                "userId", userId,
                "amount", amount,
                "timestamp", LocalDateTime.now()
        );
        kafkaTemplate.send("wallet.money.added", userId, event);
    }

    public void publishMoneyDebited(String userId, BigDecimal amount) {
        Map<String, Object> event = Map.of(
                "eventType", "MONEY_DEBITED",
                "userId", userId,
                "amount", amount,
                "timestamp", LocalDateTime.now()
        );
        kafkaTemplate.send("wallet.money.debited", userId, event);
    }
}
