package com.interview_platform.call_service.repository;

import com.interview_platform.call_service.entity.RoomParticipant;
import com.interview_platform.call_service.utils.ParticipantRole;
import com.interview_platform.call_service.utils.ParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {

    List<RoomParticipant> findByRoomToken(String roomToken);

    List<RoomParticipant> findByRoomTokenAndStatus(String roomToken, ParticipantStatus status);

    // Find by roomToken and userId
    Optional<RoomParticipant> findByRoomTokenAndUserId(String roomToken, Long userId);

    // Count by roomToken and status
    long countByRoomTokenAndStatus(String roomToken, ParticipantStatus status);

//    List<RoomParticipant> findByUserId(String userId);

//    long countByRoomId(UUID roomId);


//    List<RoomParticipant> findByRoomIdAndStatus(String roomToken, ParticipantStatus status);

    // Find participants by role
//    List<RoomParticipant> findByRoomIdAndRole(String roomToken, ParticipantRole role);

    // Find active participants
//    @Query("SELECT p FROM RoomParticipant p WHERE p.status = 'CONNECTED'")
//    List<RoomParticipant> findAllActiveParticipants();

//    @Query("""
//        SELECT p FROM RoomParticipant p
//        WHERE p.roomToken = :roomToken
//        AND p.status = 'ACTIVE'
//    """)
//    List<RoomParticipant> findActiveParticipantsByRoomToken(@Param("roomToken") String roomToken);


//    // Delete participants by room
//    void deleteByRoomId(String roomToken);
//
//    // Check if user is in room
//    boolean existsByRoomIdAndUserId(String roomToken, Long userId);
}
