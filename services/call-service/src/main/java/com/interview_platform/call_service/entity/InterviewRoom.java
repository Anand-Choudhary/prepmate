package com.interview_platform.call_service.entity;


import com.interview_platform.call_service.utils.RoomStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "interview_rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewRoom extends BaseModel {

    @Column(nullable = false)
    private String bookingReference;

    @Column(unique = true, nullable = false)
    private String roomToken;

    @Column(nullable = false)
    private Long interviewerId;

    @Column(nullable = false)
    private Long intervieweeId;

    private LocalDate scheduledAt;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    @Column(nullable = false)
    private Integer scheduledDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status = RoomStatus.SCHEDULED;

    private Boolean recordingEnabled = false;

    private String recordingUrl;

    @Column(columnDefinition = "TEXT")
    private String metadata;
}
