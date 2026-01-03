package com.interview_platform.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddMoneyResponse {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String paymentUrl;
}