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
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private Long slotId;
    private Long interviewerId;
    private String interviewerName;
    private Long intervieweeId;
    private String intervieweeName;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String meetingLink;
    private String videoRoomId;
    private String notes;
    private String bookingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
