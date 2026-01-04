package com.interview_platform.interview_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSlotRequest {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private Integer durationMinutes;
}