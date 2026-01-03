package com.interview_platform.call_service.entity;


import com.interview_platform.call_service.utils.RoomStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewRoom extends BaseModel
{

    @Column(nullable = false)
    private String interviewId;  // From interview service

    @Column(unique = true, nullable = false)
    private String roomToken;

    @Column(nullable = false)
    private String interviewerId;

    @Column(nullable = false)
    private String intervieweeId;

    private LocalDateTime scheduledAt;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Column(nullable = false)
    private Integer maxDurationMinutes = 120;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.SCHEDULED;

    private Boolean recordingEnabled = false;

    private String recordingUrl;

    @Column(columnDefinition = "TEXT")
    private String metadata;
}
