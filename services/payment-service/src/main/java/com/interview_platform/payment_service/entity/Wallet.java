package com.interview_platform.payment_service.entity;

import com.interview_platform.payment_service.utils.WalletStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet extends BaseModel{

    @Column(unique = true, nullable = false)
    private String userId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus status = WalletStatus.ACTIVE;

    @Version
    @Column(nullable = false)
    private Long version = 0L;
}

