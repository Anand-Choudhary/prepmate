package com.interview_platform.interview_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescheduleRequest {
    @NotBlank(message = "New slot ID is required")
    private String newSlotId;

    private String notes;
}