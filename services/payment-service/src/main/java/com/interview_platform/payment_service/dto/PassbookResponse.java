package com.interview_platform.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassbookResponse {
    private String userId;
    private BigDecimal currentBalance;
    private List<TransactionDTO> transactions;
    private Integer totalPages;
    private Long totalElements;
}
