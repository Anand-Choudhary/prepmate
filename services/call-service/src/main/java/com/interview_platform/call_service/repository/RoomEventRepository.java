package com.interview_platform.call_service.repository;

import com.interview_platform.call_service.entity.InterviewRoom;
import com.interview_platform.call_service.entity.RoomEvent;
import com.interview_platform.call_service.utils.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RoomEventRepository extends JpaRepository<RoomEvent, Long> {

//    List<RoomEvent> findByRoomId(UUID roomId);

    List<RoomEvent> findByRoomTokenOrderByTimestampDesc(String roomToken);

//    List<RoomEvent> findByRoomIdAndEventType(UUID roomId, EventType eventType);

//    List<RoomEvent> findByUserId(String userId);

//    List<RoomEvent> findByEventType(EventType eventType);

    // Find events in time range
//    List<RoomEvent> findByRoomIdAndTimestampBetween(
//            UUID roomId, LocalDateTime start, LocalDateTime end);

    // Find recent events
//    @Query("SELECT e FROM RoomEvent e WHERE e.roomId = :roomId " +
//            "AND e.timestamp > :since ORDER BY e.timestamp DESC")
//    List<RoomEvent> findRecentEvents(
//            @Param("roomId") UUID roomId,
//            @Param("since") LocalDateTime since);
//
//    // Count events by type
//    long countByRoomIdAndEventType(UUID roomId, EventType eventType);
//
//    // Delete events by room
//    void deleteByRoomId(UUID roomId);



}
