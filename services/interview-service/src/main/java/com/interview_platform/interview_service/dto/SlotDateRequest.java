package com.interview_platform.interview_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotDateRequest
{
    @NotNull(message = "Start time is required")
    private LocalDate startDate;

    @NotNull(message = "End time is required")
    private LocalDate endDate;
}
