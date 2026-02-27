package com.interview_platform.call_service.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest
{
    @NotBlank(message = "Booking ID is required")
    private String bookingReference;

    @NotNull(message = "Interviewer ID is required")
    private Long interviewerId;

    @NotNull(message = "Interviewee ID is required")
    private Long intervieweeId;

    @NotNull(message = "Scheduled time is required")
    private LocalDate scheduledAt;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private Integer maxDurationMinutes = 120;

    private Boolean recordingEnabled = false;
}
