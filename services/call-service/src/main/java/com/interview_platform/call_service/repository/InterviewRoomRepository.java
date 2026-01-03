package com.interview_platform.call_service.repository;

import com.interview_platform.call_service.entity.InterviewRoom;
import com.interview_platform.call_service.utils.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewRoomRepository extends JpaRepository<InterviewRoom, UUID> {

    Optional<InterviewRoom> findByRoomToken(String roomToken);

    Optional<InterviewRoom> findByInterviewId(String interviewId);

    List<InterviewRoom> findByStatus(RoomStatus status);

    List<InterviewRoom> findByInterviewerId(String interviewerId);

    List<InterviewRoom> findByIntervieweeId(String intervieweeId);

    @Query("SELECT r FROM InterviewRoom r WHERE r.interviewerId = :userId OR r.intervieweeId = :userId")
    List<InterviewRoom> findByParticipantUserId(@Param("userId") String userId);

    // Find rooms scheduled between dates
    List<InterviewRoom> findByScheduledAtBetween(LocalDateTime start, LocalDateTime end);

    // Find active rooms
    @Query("SELECT r FROM InterviewRoom r WHERE r.status = 'ACTIVE'")
    List<InterviewRoom> findActiveRooms();

    // Find expired rooms (active but past max duration)
    @Query("SELECT r FROM InterviewRoom r WHERE r.status = 'ACTIVE' AND " +
            "r.startedAt < :expiryTime")
    List<InterviewRoom> findExpiredRooms(@Param("expiryTime") LocalDateTime expiryTime);

    // Check if room exists for interview
    boolean existsByInterviewId(String interviewId);
}
