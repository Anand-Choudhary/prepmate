package com.interview_platform.payment_service.controller;

import com.interview_platform.payment_service.dto.BalanceResponse;
import com.interview_platform.payment_service.dto.CreateWalletRequest;
import com.interview_platform.payment_service.dto.WalletDTO;
import com.interview_platform.payment_service.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<WalletDTO> createWallet(@RequestBody CreateWalletRequest request) {
        return ResponseEntity.ok(walletService.createWallet(request.getUserId()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletDTO> getWallet(@PathVariable String userId) {
        // Implementation
        return null;
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String userId) {
        BigDecimal balance = walletService.getBalance(userId);
        return ResponseEntity.ok(BalanceResponse.builder()
                .userId(userId)
                .balance(balance)
                .currency("INR")
                .build());
    }
}
