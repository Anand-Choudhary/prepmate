package com.interview_platform.payment_service.entity;

import com.interview_platform.payment_service.utils.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount extends BaseModel {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String accountHolderName;

    @Column(nullable = false, length = 500)
    private String accountNumberEncrypted;

    @Column(nullable = false, length = 11)
    private String ifscCode;

    @Column(nullable = false)
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column(nullable = false)
    private Boolean isPrimary = false;

}