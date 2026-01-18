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

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class TransactionController {
    private final PaymentGatewayService paymentGatewayService;
    private final BankTransferService bankTransferService;
    private final TransactionService transactionService;

    @PostMapping("/add-money")
    public ResponseEntity<ApiResponse<AddMoneyResponse>> addMoney(@Valid @RequestBody AddMoneyRequest request) throws Exception
    {
        AddMoneyResponse response = paymentGatewayService.initiateAddMoney(request);
        return ResponseEntity.ok(ApiResponse.success("Money added", response));
    }

    @PostMapping("/transfer-to-bank")
    public ResponseEntity<ApiResponse<TransferResponse>> transferToBank(@Valid @RequestBody TransferToBankRequest request) throws Exception
    {
        TransferResponse transfer = bankTransferService.initiateTransfer(request);
        return ResponseEntity.ok(ApiResponse.success("Money transferred to bank", transfer));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getTransactions(
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDTO> response = transactionService.getTransactions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Transaction details", response));

    }

    @PostMapping("/passbook")
    public ResponseEntity<ApiResponse<PassbookResponse>> getPassbook(@RequestBody PassbookRequest request)
    {
        PassbookResponse response = transactionService.getPassbook(request);
        return ResponseEntity.ok(ApiResponse.success("Passbook details", response));
    }
}
