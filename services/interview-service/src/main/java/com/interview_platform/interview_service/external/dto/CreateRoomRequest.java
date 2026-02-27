package com.interview_platform.interview_service.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {
    private String bookingReference;
    private Long interviewerId;
    private Long intervieweeId;
    private LocalDate scheduledAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxDurationMinutes = 60;

}