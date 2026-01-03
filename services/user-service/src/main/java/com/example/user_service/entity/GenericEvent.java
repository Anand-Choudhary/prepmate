package com.example.user_service.entity;


import com.example.user_service.util.EventType;
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

    private EventType eventType;

    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    private Map<String, Object> payload;

    private Map<String, Object> metadata;
}