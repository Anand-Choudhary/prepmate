package com.interview_platform.call_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse
{
    private String roomToken;
    private String bookingReference;
    private Long interviewerId;
    private Long intervieweeId;
    private LocalDate scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String status;
    private Boolean recordingEnabled;
    private String recordingUrl;
    private LocalDateTime createdAt;
    private List<ParticipantInfo> participants;
}
