package com.interview_platform.interview_service.entity;

import com.interview_platform.interview_service.utils.SlotStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSlot extends BaseModel{

    @Column(name = "interviewer_id", nullable = false)
    private String interviewerId;

    @Column(name = "interviewee_id")
    private String intervieweeId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status;

    @Column(name = "meeting_link")
    private String meetingLink;

    @Column(name = "video_room_id")
    private String videoRoomId;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "booked_at")
    private LocalDateTime bookedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = SlotStatus.AVAILABLE;
        }
        if (version == null) {
            version = 0L;
        }
    }

    public boolean isAvailable() {
        return status == SlotStatus.AVAILABLE && startTime.isAfter(LocalDateTime.now());
    }

    public boolean canBeCancelled(int minHours) {
        return startTime.isAfter(LocalDateTime.now().plusHours(minHours));
    }

    public void markAsBooked(String intervieweeId, String videoRoomId, String meetingLink) {
        this.intervieweeId = intervieweeId;
        this.videoRoomId = videoRoomId;
        this.meetingLink = meetingLink;
        this.status = SlotStatus.BOOKED;
        this.bookedAt = LocalDateTime.now();
    }

    public void markAsCancelled() {
        this.status = SlotStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = SlotStatus.COMPLETED;
    }

    public void markAsExpired() {
        this.status = SlotStatus.EXPIRED;
    }
}
