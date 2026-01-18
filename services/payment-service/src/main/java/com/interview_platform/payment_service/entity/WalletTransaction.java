package com.interview_platform.payment_service.entity;

import com.interview_platform.payment_service.utils.TransactionCategory;
import com.interview_platform.payment_service.utils.TransactionStatus;
import com.interview_platform.payment_service.utils.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction extends BaseModel{

    @Column(nullable = false)
    private Long walletId;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionCategory transactionCategory;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(length = 255)
    private String paymentGatewayOrderId;

    @Column(length = 255)
    private String paymentGatewayTransactionId;

    @Column(length = 255)
    private String payoutId;

    @Column(length = 255)
    private UUID bankAccountId;

    @Column(unique = true)
    private String referenceId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String metadata;
}
