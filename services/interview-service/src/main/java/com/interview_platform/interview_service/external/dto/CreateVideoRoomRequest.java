package com.interview_platform.interview_service.external.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVideoRoomRequest {
    private Long hostId;
    private LocalDateTime scheduledTime;
    private Integer durationMinutes;
    private String interviewSlotId;
}