package com.interview_platform.payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassbookRequest {
    @NotBlank(message = "User ID is required")
    private String userId;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private Integer page = 0;
    private Integer size = 20;
}