package com.interview_platform.interview_service.dto;

import com.interview_platform.interview_service.utils.SlotStatus;
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
public class SlotResponse {
    private Long id;
    private Long interviewerId;
    private String interviewerName;
    private Long intervieweeId;
    private String intervieweeName;
    private String description;
    private LocalDate slotDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private SlotStatus status;
    private String meetingLink;
    private String videoRoomId;
    private LocalDateTime cancelledAt;
    private LocalDateTime bookedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}