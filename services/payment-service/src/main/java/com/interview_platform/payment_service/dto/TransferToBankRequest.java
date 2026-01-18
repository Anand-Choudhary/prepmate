package com.interview_platform.payment_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferToBankRequest {
    @NotBlank(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100.0", message = "Minimum transfer amount is ₹100")
    private BigDecimal amount;

    @NotNull(message = "Bank account ID is required")
    private Long bankAccountId;

    private String remarks;
}