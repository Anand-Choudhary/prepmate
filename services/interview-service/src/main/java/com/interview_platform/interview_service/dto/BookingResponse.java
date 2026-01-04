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
    private String id;
    private String bookingReference;
    private String slotId;
    private String interviewerId;
    private String interviewerName;
    private String intervieweeId;
    private String intervieweeName;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String meetingLink;
    private String videoRoomId;
    private String notes;
    private String bookingStatus;
    private LocalDateTime createdAt;
}
