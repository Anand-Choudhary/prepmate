package com.interview_platform.payment_service.controller;

import com.interview_platform.payment_service.dto.AddBankAccountRequest;
import com.interview_platform.payment_service.dto.BankAccountDTO;
import com.interview_platform.payment_service.service.BankAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<BankAccountDTO> addBankAccount(@Valid @RequestBody AddBankAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bankAccountService.addBankAccount(request));
    }

    @GetMapping
    public ResponseEntity<List<BankAccountDTO>> getBankAccounts(@RequestParam String userId) {
        return ResponseEntity.ok(bankAccountService.getBankAccounts(userId));
    }
}