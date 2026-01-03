package com.interview_platform.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class WebhookPayload {
    private String event;
    private String entityType;
    private Object payload;
    private LocalDateTime createdAt;
}
