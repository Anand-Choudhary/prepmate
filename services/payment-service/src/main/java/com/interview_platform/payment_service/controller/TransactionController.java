package com.interview_platform.payment_service.controller;

import com.interview_platform.payment_service.dto.*;
import com.interview_platform.payment_service.service.BankTransferService;
import com.interview_platform.payment_service.service.PaymentGatewayService;
import com.interview_platform.payment_service.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class TransactionController {
    private final PaymentGatewayService paymentGatewayService;
    private final BankTransferService bankTransferService;
    private final TransactionService transactionService;

    @PostMapping("/add-money")
    public ResponseEntity<AddMoneyResponse> addMoney(@Valid @RequestBody AddMoneyRequest request) throws Exception {
        return ResponseEntity.ok(paymentGatewayService.initiateAddMoney(request));
    }

    @PostMapping("/transfer-to-bank")
    public ResponseEntity<TransferResponse> transferToBank(@Valid @RequestBody TransferToBankRequest request) throws Exception {
        return ResponseEntity.ok(bankTransferService.initiateTransfer(request));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionDTO>> getTransactions(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(transactionService.getTransactions(userId, pageable));
    }

    @PostMapping("/passbook")
    public ResponseEntity<PassbookResponse> getPassbook(@RequestBody PassbookRequest request) {
        return ResponseEntity.ok(transactionService.getPassbook(request));
    }
}
