package com.interview_platform.call_service.repository;

import com.interview_platform.call_service.dto.ParticipantInfo;
import com.interview_platform.call_service.entity.RoomParticipant;
import com.interview_platform.call_service.utils.ParticipantRole;
import com.interview_platform.call_service.utils.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {

    List<RoomParticipant> findByRoomId(UUID roomId);

    Optional<RoomParticipant> findByRoomIdAndUserId(UUID roomId, String userId);

    List<RoomParticipant> findByUserId(String userId);

    long countByRoomId(UUID roomId);

    long countByRoomIdAndStatus(UUID roomId, ParticipantStatus status);

    List<RoomParticipant> findByRoomIdAndStatus(UUID roomId, ParticipantStatus status);

    // Find participants by role
    List<RoomParticipant> findByRoomIdAndRole(UUID roomId, ParticipantRole role);

    // Find active participants
    @Query("SELECT p FROM RoomParticipant p WHERE p.status = 'CONNECTED'")
    List<RoomParticipant> findAllActiveParticipants();

    // Delete participants by room
    void deleteByRoomId(UUID roomId);

    // Check if user is in room
    boolean existsByRoomIdAndUserId(UUID roomId, String userId);
}
