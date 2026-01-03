package com.interview_platform.payment_service.entity;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhook extends BaseModel
{

    @Column(nullable = false, length = 50)
    private String webhookType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(length = 500)
    private String signature;

    @Column(nullable = false)
    private Boolean processed = false;

    private LocalDateTime processedAt;

}

