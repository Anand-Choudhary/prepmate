package com.interview_platform.call_service.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest
{
    @NotBlank(message = "Interview ID is required")
    private String interviewId;

    @NotBlank(message = "Interviewer ID is required")
    private String interviewerId;

    @NotBlank(message = "Interviewee ID is required")
    private String intervieweeId;

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;

    private Integer maxDurationMinutes = 120;

    private Boolean recordingEnabled = false;
}
