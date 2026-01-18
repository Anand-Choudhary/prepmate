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
public class BankAccountDTO {
    private Long id;
    private Long userId;
    private String accountHolderName;
    private String accountNumberMasked;
    private String ifscCode;
    private String bankName;
    private String accountType;
    private Boolean isVerified;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
}