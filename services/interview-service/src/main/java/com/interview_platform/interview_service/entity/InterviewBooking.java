package com.interview_platform.interview_service.entity;


import com.interview_platform.interview_service.utils.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "interview_booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewBooking extends BaseModel
{
    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(name = "interviewer_id", nullable = false)
    private Long interviewerId;

    @Column(name = "interviewee_id", nullable = false)
    private Long intervieweeId;

    @Column(name = "meeting_link")
    private String meetingLink;

    @Column(name = "room_token")
    private String roomToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false)
    private BookingStatus bookingStatus;

    @Column(name = "booking_reference", unique = true, nullable = false)
    private String bookingReference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "reminder_sent")
    private Boolean reminderSent;

    @PrePersist
    public void prePersist() {
        if (bookingReference == null) {
            bookingReference = generateBookingReference();
        }
        if (bookingStatus == null) {
            bookingStatus = BookingStatus.PENDING;
        }
        if (reminderSent == null) {
            reminderSent = false;
        }
    }

    private String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
