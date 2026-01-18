package com.interview_platform.payment_service.service;

import com.interview_platform.payment_service.dto.TransferResponse;
import com.interview_platform.payment_service.dto.TransferToBankRequest;
import com.interview_platform.payment_service.entity.BankAccount;
import com.interview_platform.payment_service.repository.BankAccountRepository;
import com.interview_platform.payment_service.security.UserContext;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankTransferService {

    private final WalletService walletService;
    private final BankAccountRepository bankAccountRepository;
    private final RazorpayClient razorpayClient;

    public TransferResponse initiateTransfer(TransferToBankRequest request) throws Exception {
        if (request.getAmount().compareTo(new BigDecimal("1000")) < 0) {
            throw new RuntimeException("Minimum transfer amount is ₹1000");
        }
        Long userId = UserContext.getUserId();

        BigDecimal balance = walletService.getBalance(userId);
        if (balance.compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        BankAccount bankAccount = bankAccountRepository.findById(request.getBankAccountId())
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        String referenceId = UUID.randomUUID().toString();
        walletService.debitWallet(request.getUserId(), request.getAmount(),
                referenceId, "Transfer to bank");

        // Create payout (placeholder - implement actual Razorpay payout)
        String payoutId = "payout_" + UUID.randomUUID().toString();

        return TransferResponse.builder()
                .transactionId(referenceId)
                .payoutId(payoutId)
                .amount(request.getAmount())
                .status("PENDING")
                .message("Transfer initiated successfully")
                .build();
    }
}