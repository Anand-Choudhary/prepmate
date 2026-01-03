package com.interview_platform.payment_service.service;

import com.interview_platform.payment_service.dto.WalletDTO;
import com.interview_platform.payment_service.entity.Wallet;
import com.interview_platform.payment_service.entity.WalletTransaction;
import com.interview_platform.payment_service.repository.WalletRepository;
import com.interview_platform.payment_service.repository.WalletTransactionRepository;
import com.interview_platform.payment_service.utils.TransactionCategory;
import com.interview_platform.payment_service.utils.TransactionStatus;
import com.interview_platform.payment_service.utils.TransactionType;
import com.interview_platform.payment_service.utils.WalletStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
//    private final EventPublisherService eventPublisher;

    @Transactional
    public WalletDTO createWallet(String userId) {
        if (walletRepository.existsByUserId(userId)) {
            throw new RuntimeException("Wallet already exists for user: " + userId);
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .currency("INR")
                .status(WalletStatus.ACTIVE)
                .build();

        wallet = walletRepository.save(wallet);
//        eventPublisher.publishWalletCreated(userId);

        log.info("Wallet created for user: {}", userId);
        return mapToDTO(wallet);
    }

    public BigDecimal getBalance(String userId) {
        String cacheKey = "wallet:" + userId + ":balance";
        BigDecimal cachedBalance = (BigDecimal) redisTemplate.opsForValue().get(cacheKey);

        if (cachedBalance != null) {
            return cachedBalance;
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        redisTemplate.opsForValue().set(cacheKey, wallet.getBalance(), 5, TimeUnit.MINUTES);
        return wallet.getBalance();
    }

    @Transactional
    public void creditWallet(String userId, BigDecimal amount, String referenceId, String description) {
        if (transactionRepository.existsByReferenceId(referenceId)) {
            log.warn("Duplicate transaction: {}", referenceId);
            return;
        }

        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        createTransaction(wallet.getId(), userId, TransactionType.CREDIT,
                TransactionCategory.ADD_MONEY, amount, balanceBefore,
                balanceAfter, TransactionStatus.SUCCESS, referenceId, description);

        redisTemplate.delete("wallet:" + userId + ":balance");
//        eventPublisher.publishMoneyAdded(userId, amount);
    }

    @Transactional
    public void debitWallet(String userId, BigDecimal amount, String referenceId, String description) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        wallet.setBalance(balanceAfter);
        walletRepository.save(wallet);

        createTransaction(wallet.getId(), userId, TransactionType.DEBIT,
                TransactionCategory.BANK_TRANSFER, amount, balanceBefore,
                balanceAfter, TransactionStatus.SUCCESS, referenceId, description);

        redisTemplate.delete("wallet:" + userId + ":balance");
//        eventPublisher.publishMoneyDebited(userId, amount);
    }

    private void createTransaction(Long walletId, String userId, TransactionType type,
                                   TransactionCategory category, BigDecimal amount,
                                   BigDecimal balanceBefore, BigDecimal balanceAfter,
                                   TransactionStatus status, String referenceId, String description) {
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(walletId)
                .userId(userId)
                .transactionType(type)
                .transactionCategory(category)
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .status(status)
                .referenceId(referenceId)
                .description(description)
                .build();
        transactionRepository.save(transaction);
    }

    private WalletDTO mapToDTO(Wallet wallet) {
        return WalletDTO.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus().name())
                .createdAt(wallet.getCreatedAt())
                .build();
    }
}
