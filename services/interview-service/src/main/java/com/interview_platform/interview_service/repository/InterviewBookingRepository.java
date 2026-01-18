package com.interview_platform.interview_service.repository;

import com.interview_platform.interview_service.entity.InterviewBooking;
import com.interview_platform.interview_service.utils.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewBookingRepository extends JpaRepository<InterviewBooking, Long> {

    // Find booking by reference
    Optional<InterviewBooking> findByBookingReference(String bookingReference);

    // Find booking by slot ID
    Optional<InterviewBooking> findBySlotId(Long slotId);

    // Find bookings by interviewee
    List<InterviewBooking> findByIntervieweeIdAndBookingStatusInOrderByCreatedAtDesc(
            Long intervieweeId,
            List<BookingStatus> statuses
    );

    // Find bookings by interviewer
    List<InterviewBooking> findByInterviewerIdAndBookingStatusInOrderByCreatedAtDesc(
            Long interviewerId,
            List<BookingStatus> statuses
    );

    // Check if booking exists for slot
    boolean existsBySlotIdAndBookingStatus(Long slotId, BookingStatus status);

    // Find bookings that need reminders
    @Query("SELECT b FROM InterviewBooking b " +
            "JOIN InterviewSlot s ON b.slotId = s.id " +
            "WHERE b.bookingStatus = 'CONFIRMED' " +
            "AND b.reminderSent = false " +
            "AND s.startTime BETWEEN :start AND :end")
    List<InterviewBooking> findBookingsNeedingReminder(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Count bookings by interviewee in date range
    @Query("SELECT COUNT(b) FROM InterviewBooking b " +
            "WHERE b.intervieweeId = :intervieweeId " +
            "AND b.bookingStatus = 'CONFIRMED' " +
            "AND b.createdAt BETWEEN :start AND :end")
    long countBookingsByIntervieweeInDateRange(
            @Param("intervieweeId") Long intervieweeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}