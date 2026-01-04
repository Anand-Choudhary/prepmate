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
public class VideoRoomResponse {
    private String roomId;
    private String meetingLink;
    private String hostJoinLink;
    private String guestJoinLink;
    private LocalDateTime scheduledTime;
    private String status;
}
