package com.interview_platform.payment_service.controller;

import com.interview_platform.payment_service.dto.ApiResponse;
import com.interview_platform.payment_service.dto.BalanceResponse;
//import com.interview_platform.payment_service.dto.CreateWalletRequest;
import com.interview_platform.payment_service.dto.WalletDTO;
import com.interview_platform.payment_service.security.UserContext;
import com.interview_platform.payment_service.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<WalletDTO>> createWallet()
    {
        WalletDTO wallet = walletService.createWallet();

        return ResponseEntity.ok(ApiResponse.success("Wallet created", wallet));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletDTO> getWallet(@PathVariable Long userId) {
        // Implementation
        return null;
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance()
    {
        Long userId = UserContext.getUserId();
        BigDecimal balance = walletService.getBalance(userId);
        return ResponseEntity.ok(ApiResponse.success("Wallet created", BalanceResponse.builder()
                .userId(userId)
                .balance(balance)
                .currency("INR")
                .build()));
    }
}
