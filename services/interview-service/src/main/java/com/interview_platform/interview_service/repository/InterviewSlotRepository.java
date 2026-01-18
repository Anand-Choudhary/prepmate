package com.interview_platform.interview_service.repository;

import com.interview_platform.interview_service.entity.InterviewSlot;
import com.interview_platform.interview_service.utils.SlotStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewSlotRepository extends JpaRepository<InterviewSlot, Long> {

    List<InterviewSlot> findByInterviewerIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
            String interviewerId,
            SlotStatus status,
            LocalDateTime after
    );

    @Query("SELECT s FROM InterviewSlot s WHERE s.status = :status AND s.startTime > :after ORDER BY s.startTime ASC")
    List<InterviewSlot> findAvailableSlots(
            @Param("status") SlotStatus status,
            @Param("after") LocalDateTime after
    );

    @Query("SELECT ia FROM InterviewSlot ia " +
            "WHERE ia.interviewerId = :interviewerId " +
            "AND ia.slotDate BETWEEN :startDate AND :endDate " +
            "AND ia.isActive = true " +
            "ORDER BY ia.availabilityDate, ia.startTime")
    List<InterviewSlot> findAvailableSlotsForInterviewer
            (Long interviewerId,
             LocalDate startDate,
             LocalDate endDate);

    // Find slot with pessimistic write lock (for booking)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InterviewSlot s WHERE s.id = :slotId")
    Optional<InterviewSlot> findByIdWithLock(@Param("slotId") String slotId);

    // Find slot by ID and status with optimistic lock check
    @Query("SELECT s FROM InterviewSlot s WHERE s.id = :slotId AND s.status = :status AND s.version = :version")
    Optional<InterviewSlot> findByIdAndStatusAndVersion(
            @Param("slotId") String slotId,
            @Param("status") SlotStatus status,
            @Param("version") Long version
    );

    // Check if slot exists and is available
    @Query("SELECT COUNT(s) > 0 FROM InterviewSlot s WHERE s.id = :slotId AND s.status = 'AVAILABLE' AND s.startTime > :minStartTime")
    boolean existsAvailableSlot(@Param("slotId") String slotId, @Param("minStartTime") LocalDateTime minStartTime);

    // Find overlapping slots for an interviewer
    @Query("SELECT s FROM InterviewSlot s WHERE s.interviewerId = :interviewerId " +
            "AND s.status NOT IN ('CANCELLED', 'EXPIRED') " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<InterviewSlot> findOverlappingSlots(
            @Param("interviewerId") String interviewerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // Count bookings for an interviewee on a specific date
    @Query("SELECT COUNT(s) FROM InterviewSlot s WHERE s.intervieweeId = :intervieweeId " +
            "AND s.status = 'BOOKED' " +
            "AND DATE(s.startTime) = DATE(:date)")
    long countBookingsForIntervieweeOnDate(
            @Param("intervieweeId") Long intervieweeId,
            @Param("date") LocalDateTime date
    );

    // Find slots by interviewee
    List<InterviewSlot> findByIntervieweeIdAndStatusInOrderByStartTimeAsc(
            String intervieweeId,
            List<SlotStatus> statuses
    );

    // Find slots by interviewer
    List<InterviewSlot> findByInterviewerIdAndStatusInOrderByStartTimeAsc(
            String interviewerId,
            List<SlotStatus> statuses
    );

    // Find expired slots that need to be updated
    @Query("SELECT s FROM InterviewSlot s WHERE s.status = 'AVAILABLE' " +
            "AND s.startTime < :expiryTime")
    List<InterviewSlot> findExpiredSlots(@Param("expiryTime") LocalDateTime expiryTime);

    // Update slot status (for batch operations)
    @Modifying
    @Query("UPDATE InterviewSlot s SET s.status = :newStatus WHERE s.id IN :slotIds")
    int updateSlotStatus(@Param("slotIds") List<String> slotIds, @Param("newStatus") SlotStatus newStatus);

    // Check for duplicate booking attempt
    @Query("SELECT COUNT(s) > 0 FROM InterviewSlot s WHERE s.intervieweeId = :intervieweeId " +
            "AND s.id = :slotId AND s.status = 'BOOKED'")
    boolean existsByIntervieweeIdAndSlotId(
            @Param("intervieweeId") Long intervieweeId,
            @Param("slotId") Long slotId
    );

    // Find upcoming interviews for notifications
    @Query("SELECT s FROM InterviewSlot s WHERE s.status = 'BOOKED' " +
            "AND s.startTime BETWEEN :start AND :end")
    List<InterviewSlot> findUpcomingInterviews(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}