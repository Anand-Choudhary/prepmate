package com.interview_platform.interview_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DateSlotRequest {

    @NotNull(message = "Availability date is required")
    @FutureOrPresent(message = "Availability date must be today or in the future")
    private LocalDate date;

    @NotEmpty(message = "At least one time slot is required for each date")
    @Valid
    private List<TimeSlotRequest> timeSlots;

    private String notes;
}