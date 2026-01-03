package com.interview_platform.payment_service.service;

import com.interview_platform.payment_service.dto.AddBankAccountRequest;
import com.interview_platform.payment_service.dto.BankAccountDTO;
import com.interview_platform.payment_service.entity.BankAccount;
import com.interview_platform.payment_service.repository.BankAccountRepository;
import com.interview_platform.payment_service.utils.AccountType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private static final String ENCRYPTION_KEY = "YourSecretKey123"; // Use proper key management

    @Transactional
    public BankAccountDTO addBankAccount(AddBankAccountRequest request) {
        String encryptedAccount = encrypt(request.getAccountNumber());

        BankAccount bankAccount = BankAccount.builder()
                .userId(request.getUserId())
                .accountHolderName(request.getAccountHolderName())
                .accountNumberEncrypted(encryptedAccount)
                .ifscCode(request.getIfscCode())
                .bankName(request.getBankName())
                .accountType(AccountType.valueOf(request.getAccountType()))
                .isVerified(false)
                .isPrimary(false)
                .build();

        bankAccount = bankAccountRepository.save(bankAccount);
        return mapToDTO(bankAccount);
    }

    public List<BankAccountDTO> getBankAccounts(String userId) {
        return bankAccountRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private String encrypt(String data) {
        // Implement AES encryption
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    private BankAccountDTO mapToDTO(BankAccount account) {
        return BankAccountDTO.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .accountHolderName(account.getAccountHolderName())
                .accountNumberMasked("XXXX" + account.getAccountNumberEncrypted().substring(0, 4))
                .ifscCode(account.getIfscCode())
                .bankName(account.getBankName())
                .accountType(account.getAccountType().name())
                .isVerified(account.getIsVerified())
                .isPrimary(account.getIsPrimary())
                .createdAt(account.getCreatedAt())
                .build();
    }
}