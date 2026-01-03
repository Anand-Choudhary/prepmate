package com.interview_platform.call_service.service;

import com.interview_platform.call_service.dto.*;
import com.interview_platform.call_service.entity.InterviewRoom;
import com.interview_platform.call_service.entity.RoomEvent;
import com.interview_platform.call_service.entity.RoomParticipant;
import com.interview_platform.call_service.repository.InterviewRoomRepository;
import com.interview_platform.call_service.repository.RoomEventRepository;
import com.interview_platform.call_service.repository.RoomParticipantRepository;
import com.interview_platform.call_service.utils.EventType;
import com.interview_platform.call_service.utils.ParticipantRole;
import com.interview_platform.call_service.utils.ParticipantStatus;
import com.interview_platform.call_service.utils.RoomStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCallService {

    private final InterviewRoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final RoomEventRepository eventRepository;
    private final RedisTemplate<String, Object> redisTemplate;
//    private final EventPublisherService eventPublisher;

    private static final String REDIS_ROOM_PREFIX = "room:";
    private static final String REDIS_PARTICIPANT_PREFIX = "participant:";

    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        log.info("Creating room for interview: {}", request.getInterviewId());

        // Generate unique room token
        String roomToken = UUID.randomUUID().toString();

        // Create room entity
        InterviewRoom room = InterviewRoom.builder()
                .interviewId(request.getInterviewId())
                .roomToken(roomToken)
                .interviewerId(request.getInterviewerId())
                .intervieweeId(request.getIntervieweeId())
                .scheduledAt(request.getScheduledAt())
                .maxDurationMinutes(request.getMaxDurationMinutes())
                .recordingEnabled(request.getRecordingEnabled())
                .status(RoomStatus.SCHEDULED)
                .build();

        room = roomRepository.save(room);

        // Store in Redis for quick access
        String redisKey = REDIS_ROOM_PREFIX + room.getId();
        redisTemplate.opsForValue().set(redisKey, room, 24, TimeUnit.HOURS);

        // Log event
        logEvent(room.getId(), EventType.ROOM_CREATED, null, null);

        // Publish event to Kafka
//        eventPublisher.publishRoomCreated(room.getId().toString(), request.getInterviewId(),
//                request.getInterviewerId(), request.getIntervieweeId());

        log.info("Room created successfully: {} with token: {}", room.getId(), roomToken);

        return mapToRoomResponse(room, Collections.emptyList());
    }

    @Transactional
    public JoinRoomResponse joinRoom(JoinRoomRequest request) {
        log.info("User {} attempting to join room with token: {}",
                request.getUserId(), request.getRoomToken());

        // Find room by token
        InterviewRoom room = roomRepository.findByRoomToken(request.getRoomToken())
                .orElseThrow(() -> new RuntimeException("Invalid room token"));

        // Validate user is allowed to join
        if (!canUserJoinRoom(room, request.getUserId())) {
            throw new RuntimeException("User not authorized to join this room");
        }

        // Determine participant role
        ParticipantRole role = determineParticipantRole(room, request.getUserId());

        // Create or update participant
        RoomParticipant participant = RoomParticipant.builder()
                .roomId(room.getId())
                .userId(request.getUserId())
                .role(role)
                .joinedAt(LocalDateTime.now())
                .status(ParticipantStatus.CONNECTED)
                .build();

        participantRepository.save(participant);

        // Update room status
        if (room.getStatus() == RoomStatus.SCHEDULED) {
            room.setStatus(RoomStatus.WAITING);
        }

        // If both participants joined, activate room
        long participantCount = participantRepository.countByRoomIdAndStatus(
                room.getId(), ParticipantStatus.CONNECTED);

        if (participantCount >= 2 && room.getStatus() == RoomStatus.WAITING) {
            room.setStatus(RoomStatus.ACTIVE);
            room.setStartedAt(LocalDateTime.now());

            // Publish room started event
//            eventPublisher.publishRoomStarted(room.getId().toString(),
//                    room.getInterviewId());
        }

        roomRepository.save(room);

        // Store participant in Redis
        String redisKey = REDIS_PARTICIPANT_PREFIX + room.getId() + ":" + request.getUserId();
        redisTemplate.opsForValue().set(redisKey, participant, 24, TimeUnit.HOURS);

        // Log event
        logEvent(room.getId(), EventType.PARTICIPANT_JOINED, request.getUserId(), null);

        // Publish participant joined event
//        eventPublisher.publishParticipantJoined(room.getId().toString(),
//                request.getUserId(), role.name());

        // Get other participants
        List<ParticipantInfo> otherParticipants = getOtherParticipants(room.getId(),
                request.getUserId());

        log.info("User {} joined room {} as {}", request.getUserId(), room.getId(), role);

        return JoinRoomResponse.builder()
                .roomId(room.getId())
                .userId(request.getUserId())
                .role(role.name())
                .status(participant.getStatus().name())
                .iceServers(getIceServers())
                .otherParticipants(otherParticipants)
                .build();
    }

    @Transactional
    public void leaveRoom(UUID roomId, String userId) {
        log.info("User {} leaving room {}", userId, roomId);

        // Update participant status
        participantRepository.findByRoomIdAndUserId(roomId, userId)
                .ifPresent(participant -> {
                    participant.setStatus(ParticipantStatus.DISCONNECTED);
                    participant.setLeftAt(LocalDateTime.now());
                    participantRepository.save(participant);
                });

        // Remove from Redis
        String redisKey = REDIS_PARTICIPANT_PREFIX + roomId + ":" + userId;
        redisTemplate.delete(redisKey);

        // Log event
        logEvent(roomId, EventType.PARTICIPANT_LEFT, userId, null);

        // Publish participant left event
//        eventPublisher.publishParticipantLeft(roomId.toString(), userId);

        // Check if all participants left
        long activeParticipants = participantRepository.countByRoomIdAndStatus(
                roomId, ParticipantStatus.CONNECTED);

        if (activeParticipants == 0) {
            endRoom(roomId);
        }

        log.info("User {} left room {}", userId, roomId);
    }

    @Transactional
    public void endRoom(UUID roomId) {
        log.info("Ending room: {}", roomId);

        InterviewRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setStatus(RoomStatus.COMPLETED);
        room.setEndedAt(LocalDateTime.now());
        roomRepository.save(room);

        // Log event
        logEvent(roomId, EventType.CALL_ENDED, null, null);

        // Publish room ended event
//        eventPublisher.publishRoomEnded(roomId.toString(), room.getInterviewId());

        // Clean up Redis
        redisTemplate.delete(REDIS_ROOM_PREFIX + roomId);

        log.info("Room {} ended", roomId);
    }

    public RoomResponse getRoomDetails(UUID roomId) {
        InterviewRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<ParticipantInfo> participants = participantRepository.findByRoomId(roomId)
                .stream()
                .map(this::mapToParticipantInfo)
                .collect(Collectors.toList());

        return mapToRoomResponse(room, participants);
    }

    // ========== Helper Methods ==========

    private boolean canUserJoinRoom(InterviewRoom room, String userId) {
        return userId.equals(room.getInterviewerId()) ||
                userId.equals(room.getIntervieweeId());
    }

    private ParticipantRole determineParticipantRole(InterviewRoom room, String userId) {
        if (userId.equals(room.getInterviewerId())) {
            return ParticipantRole.INTERVIEWER;
        } else if (userId.equals(room.getIntervieweeId())) {
            return ParticipantRole.INTERVIEWEE;
        }
        return ParticipantRole.OBSERVER;
    }

    private List<ParticipantInfo> getOtherParticipants(UUID roomId, String currentUserId) {
        return participantRepository.findByRoomId(roomId).stream()
                .filter(p -> !p.getUserId().equals(currentUserId))
                .filter(p -> p.getStatus() == ParticipantStatus.CONNECTED)
                .map(this::mapToParticipantInfo)
                .collect(Collectors.toList());
    }

    private List<String> getIceServers() {
        return Arrays.asList(
                "stun:stun.l.google.com:19302",
                "stun:stun1.l.google.com:19302"
        );
    }

    private void logEvent(UUID roomId, EventType eventType, String userId, String metadata) {
        RoomEvent event = RoomEvent.builder()
                .roomId(roomId)
                .eventType(eventType)
                .userId(userId)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();

        eventRepository.save(event);
    }

    private RoomResponse mapToRoomResponse(InterviewRoom room, List<ParticipantInfo> participants) {
        return RoomResponse.builder()
                .roomId(room.getId())
                .roomToken(room.getRoomToken())
                .interviewId(room.getInterviewId())
                .interviewerId(room.getInterviewerId())
                .intervieweeId(room.getIntervieweeId())
                .scheduledAt(room.getScheduledAt())
                .startedAt(room.getStartedAt())
                .endedAt(room.getEndedAt())
                .status(room.getStatus().name())
                .recordingEnabled(room.getRecordingEnabled())
                .recordingUrl(room.getRecordingUrl())
                .createdAt(room.getCreatedAt())
                .participants(participants)
                .build();
    }

    private ParticipantInfo mapToParticipantInfo(RoomParticipant participant) {
        // In production, fetch user details from User Service
        return ParticipantInfo.builder()
                .userId(participant.getUserId())
                .name("User " + participant.getUserId()) // TODO: Fetch from User Service
                .role(participant.getRole().name())
                .status(participant.getStatus().name())
                .joinedAt(participant.getJoinedAt())
                .build();
    }

    public RoomResponse getRoomByInterviewId(String interviewId) {
        log.info("Fetching room for interview: {}", interviewId);

        InterviewRoom room = roomRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new RuntimeException("Room not found for interview"));

        List<ParticipantInfo> participants = participantRepository.findByRoomId(room.getId())
                .stream()
                .map(this::mapToParticipantInfo)
                .collect(Collectors.toList());

        return mapToRoomResponse(room, participants);
    }

    /**
     * Get all rooms for a user
     */
    public List<RoomResponse> getUserRooms(String userId) {
        log.info("Fetching rooms for user: {}", userId);

        List<InterviewRoom> rooms = roomRepository.findByParticipantUserId(userId);

        return rooms.stream()
                .map(room -> {
                    List<ParticipantInfo> participants = participantRepository.findByRoomId(room.getId())
                            .stream()
                            .map(this::mapToParticipantInfo)
                            .collect(Collectors.toList());
                    return mapToRoomResponse(room, participants);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all active rooms
     */
    public List<RoomResponse> getActiveRooms() {
        log.info("Fetching active rooms");

        List<InterviewRoom> rooms = roomRepository.findActiveRooms();

        return rooms.stream()
                .map(room -> {
                    List<ParticipantInfo> participants = participantRepository.findByRoomId(room.getId())
                            .stream()
                            .map(this::mapToParticipantInfo)
                            .collect(Collectors.toList());
                    return mapToRoomResponse(room, participants);
                })
                .collect(Collectors.toList());
    }

    /**
     * Start recording
     */
    @Transactional
    public void startRecording(UUID roomId, String userId) {
        log.info("Starting recording for room: {} by user: {}", roomId, userId);

        InterviewRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Validate user is interviewer
        if (!userId.equals(room.getInterviewerId())) {
            throw new RuntimeException("Only interviewer can start recording");
        }

        // Check if recording is enabled
        if (!room.getRecordingEnabled()) {
            throw new RuntimeException("Recording is not enabled for this room");
        }

        // Check if room is active
        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new RuntimeException("Can only record active rooms");
        }

        // Log event
        logEvent(roomId, EventType.RECORDING_STARTED, userId, null);

        // In production, start actual recording with media server
        // For now, just log the action
        log.info("Recording started for room: {}", roomId);
    }

    /**
     * Stop recording
     */
    @Transactional
    public String stopRecording(UUID roomId, String userId) {
        log.info("Stopping recording for room: {} by user: {}", roomId, userId);

        InterviewRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Validate user is interviewer
        if (!userId.equals(room.getInterviewerId())) {
            throw new RuntimeException("Only interviewer can stop recording");
        }

        // Generate recording URL (in production, this would come from media server)
        String recordingUrl = "/recordings/" + roomId + ".mp4";
        room.setRecordingUrl(recordingUrl);
        roomRepository.save(room);

        // Log event
        logEvent(roomId, EventType.RECORDING_STOPPED, userId, recordingUrl);

        log.info("Recording stopped for room: {}, URL: {}", roomId, recordingUrl);

        return recordingUrl;
    }

    /**
     * Get room events
     */
    public List<RoomEventDTO> getRoomEvents(UUID roomId) {
        log.info("Fetching events for room: {}", roomId);

        List<RoomEvent> events = eventRepository.findByRoomIdOrderByTimestampDesc(roomId);

        return events.stream()
                .map(event -> RoomEventDTO.builder()
                        .eventType(event.getEventType().name())
                        .roomId(event.getRoomId().toString())
                        .userId(event.getUserId())
                        .userName("User " + event.getUserId()) // TODO: Fetch from User Service
                        .timestamp(event.getTimestamp())
                        .metadata(event.getMetadata())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Clean up expired rooms
     * This should be called by a scheduled task
     */
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void cleanupExpiredRooms() {
        log.info("Running cleanup for expired rooms");

        LocalDateTime expiryTime = LocalDateTime.now().minusHours(2);
        List<InterviewRoom> expiredRooms = roomRepository.findExpiredRooms(expiryTime);

        for (InterviewRoom room : expiredRooms) {
            log.info("Cleaning up expired room: {}", room.getId());

            room.setStatus(RoomStatus.EXPIRED);
            room.setEndedAt(LocalDateTime.now());
            roomRepository.save(room);

            // Disconnect all participants
            List<RoomParticipant> participants = participantRepository.findByRoomId(room.getId());
            for (RoomParticipant participant : participants) {
                if (participant.getStatus() == ParticipantStatus.CONNECTED) {
                    participant.setStatus(ParticipantStatus.DISCONNECTED);
                    participant.setLeftAt(LocalDateTime.now());
                    participantRepository.save(participant);
                }
            }

            // Clean up Redis
            redisTemplate.delete(REDIS_ROOM_PREFIX + room.getId());

            // Publish event
//            eventPublisher.publishRoomEnded(room.getId().toString(), room.getInterviewId());
        }

        log.info("Cleaned up {} expired rooms", expiredRooms.size());
    }


}