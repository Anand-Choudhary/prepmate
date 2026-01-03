package com.interview_platform.payment_service.service;

import com.interview_platform.payment_service.dto.PassbookRequest;
import com.interview_platform.payment_service.dto.PassbookResponse;
import com.interview_platform.payment_service.dto.TransactionDTO;
import com.interview_platform.payment_service.entity.WalletTransaction;
import com.interview_platform.payment_service.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final WalletTransactionRepository transactionRepository;

    public Page<TransactionDTO> getTransactions(String userId, Pageable pageable) {
        Page<WalletTransaction> transactions =
                transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return transactions.map(this::mapToDTO);
    }

    public PassbookResponse getPassbook(PassbookRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<WalletTransaction> transactions;

        if (request.getFromDate() != null && request.getToDate() != null) {
            List<WalletTransaction> txnList = transactionRepository
                    .findByUserIdAndCreatedAtBetween(request.getUserId(),
                            request.getFromDate(),
                            request.getToDate());
            transactions = new PageImpl<>(txnList, pageable, txnList.size());
        } else {
            transactions = transactionRepository
                    .findByUserIdOrderByCreatedAtDesc(request.getUserId(), pageable);
        }

        return PassbookResponse.builder()
                .userId(request.getUserId())
                .transactions(transactions.getContent().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toList()))
                .totalPages(transactions.getTotalPages())
                .totalElements(transactions.getTotalElements())
                .build();
    }

    private TransactionDTO mapToDTO(WalletTransaction txn) {
        return TransactionDTO.builder()
                .id(txn.getId())
                .userId(txn.getUserId())
                .transactionType(txn.getTransactionType().name())
                .transactionCategory(txn.getTransactionCategory().name())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .balanceBefore(txn.getBalanceBefore())
                .balanceAfter(txn.getBalanceAfter())
                .status(txn.getStatus().name())
                .description(txn.getDescription())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}