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
public class JoinRoomResponse {
    private String roomToken;
    private Long userId;
    private String role;
    private String status;
    private String roomStatus;
    private Boolean billingEnabled;
    private List<String> iceServers;
    private List<ParticipantInfo> otherParticipants;
    private LocalDateTime startedAt;
    private LocalDate scheduledAt;
    private String message;
}
