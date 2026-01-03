package com.prepmate.consumer_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenericEvent {

    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    private String eventType;  // USER_REGISTERED, PAYMENT_SUCCESS, etc.

    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private Map<String, Object> payload;  // Event-specific data

    private Map<String, Object> metadata; // Source, version, etc.
}
