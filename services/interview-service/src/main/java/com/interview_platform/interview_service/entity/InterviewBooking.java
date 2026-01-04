package com.interview_platform.interview_service.entity;


import com.interview_platform.interview_service.utils.BookingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewBooking extends BaseModel
{
    @Column(name = "slot_id", nullable = false)
    private String slotId;

    @Column(name = "interviewer_id", nullable = false)
    private String interviewerId;

    @Column(name = "interviewee_id", nullable = false)
    private String intervieweeId;

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
            bookingStatus = BookingStatus.CONFIRMED;
        }
        if (reminderSent == null) {
            reminderSent = false;
        }
    }

    private String generateBookingReference() {
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
