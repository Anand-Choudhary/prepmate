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
public class BookingSuccessResponse {
    private String bookingId;
    private String bookingReference;
    private String slotId;
    private String interviewerId;
    private String intervieweeId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String meetingLink;
    private String videoRoomId;
    private String message;
    private LocalDateTime bookedAt;
}
