package com.interview_platform.call_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomResponse {
    private UUID roomId;
    private String userId;
    private String role;  // INTERVIEWER or INTERVIEWEE
    private String status;
    private List<String> iceServers;
    private List<ParticipantInfo> otherParticipants;
}
