package com.interview_platform.interview_service.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomResponse {
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
}
