package com.interview_platform.interview_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
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
public class CreateAvailabilityRequest {

//    @NotNull(message = "Interviewer ID is required")
//    private Long interviewerId;

    @NotEmpty(message = "At least one date slot is required")
    @Valid
    private List<DateSlotRequest> dateSlots;

    private String notes;
}