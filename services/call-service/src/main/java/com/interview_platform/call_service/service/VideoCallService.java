package com.interview_platform.call_service.service;

import com.interview_platform.call_service.dto.*;
import com.interview_platform.call_service.entity.InterviewRoom;
import com.interview_platform.call_service.entity.RoomEvent;
import com.interview_platform.call_service.entity.RoomParticipant;
import com.interview_platform.call_service.exception.BadRequestException;
import com.interview_platform.call_service.exception.ResourceNotFoundException;
import com.interview_platform.call_service.exception.RoomAccessException;
import com.interview_platform.call_service.mapper.EntityMapper;
import com.interview_platform.call_service.repository.InterviewRoomRepository;
import com.interview_platform.call_service.repository.RoomEventRepository;
import com.interview_platform.call_service.repository.RoomParticipantRepository;
import com.interview_platform.call_service.utils.EventType;
import com.interview_platform.call_service.utils.ParticipantRole;
import com.interview_platform.call_service.utils.ParticipantStatus;
import com.interview_platform.call_service.utils.RoomStatus;
import jakarta.ws.rs.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoCallService {

    private final InterviewRoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final RoomEventRepository eventRepository;
//    private final RoomService roomService;
    private final QualityCacheService qualityCacheService;
//    private final BillingService billingService;
    private final EntityMapper entityMapper;
    private final RedisTemplate<String, Object> redisTemplate;
//    private final EventPublisherService eventPublisher;

    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request)
    {
        try{
            log.info("Creating room for interview: {}", request.getBookingReference());

            String roomToken = "RT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            InterviewRoom room = InterviewRoom.builder()
                    .bookingReference(request.getBookingReference())
                    .roomToken(roomToken)
                    .interviewerId(request.getInterviewerId())
                    .intervieweeId(request.getIntervieweeId())
                    .scheduledAt(request.getScheduledAt())
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .scheduledDurationMinutes(request.getMaxDurationMinutes())
                    .recordingEnabled(request.getRecordingEnabled())
                    .status(RoomStatus.SCHEDULED)
                    .build();

            room = roomRepository.save(room);

            logEvent(room.getRoomToken(), EventType.ROOM_CREATED, request.getIntervieweeId(), null);

            // Publish event to Kafka
//        eventPublisher.publishRoomCreated(room.getId().toString(), request.getInterviewId(),
//                request.getInterviewerId(), request.getIntervieweeId());

            log.info("Room created successfully: {} with token: {}", room.getId(), roomToken);

            return entityMapper.toRoomResponse(room, Collections.emptyList());
        } catch (Exception e) {
            log.error("Unexpected error while creating room for bookingReference: {}",
                    request.getBookingReference(), e);
            throw new InternalServerErrorException("Unexpected error while creating room");
        }

    }

    @Transactional
    public JoinRoomResponse joinRoom(JoinRoomRequest request, String webSocketSession) {
        log.info("User {} attempting to join room with token: {}",
                request.getUserId(), request.getRoomToken());

        // STEP 1: Load and validate room
        InterviewRoom room = loadAndValidateRoom(request.getRoomToken(), request.getUserId());

        // STEP 2: Check for rejoin scenario (user already in room)
//        Optional<JoinRoomResponse> rejoinResponse = handleRejoinScenario(room, request.getUserId(), webSocketSession);
//        if (rejoinResponse.isPresent()) {
//            return rejoinResponse.get();
//        }

        // STEP 3: Initialize room if first user joining
//        firstUserJoined(room, request.getUserId());

        // STEP 4: Create and add participant
        RoomParticipant participant = createAndAddParticipant(room, request.getUserId());

        // STEP 5: Activate room and start billing if both users present
        activateRoomIfReady(room, participant);

        // STEP 6: Build and return response
        return buildJoinResponse(room, participant);
    }



    private InterviewRoom loadAndValidateRoom(String roomToken, Long userId) {
        // Find room
        InterviewRoom room = roomRepository.findByRoomToken(roomToken)
                .orElseThrow(() -> new RuntimeException("Invalid room token"));

        // Validate user authorization
        validateUserAuthorization(room, userId);

        // Validate room capacity
//        validateRoomCapacity(room);

        // Validate join timing
//        validateJoinTiming(room,userId);

        // Validate room status
        validateRoomStatus(room);

        return room;
    }


    private void validateUserAuthorization(InterviewRoom room, Long userId) {
        boolean isInterviewer = userId.equals(room.getInterviewerId());
        boolean isInterviewee = userId.equals(room.getIntervieweeId());

        if (!isInterviewer && !isInterviewee) {
            log.warn("User {} not authorized to join room {}", userId, room.getRoomToken());
            throw new RuntimeException("User not authorized to join this room");
        }
    }

    private void validateRoomCapacity(InterviewRoom room) {
        List<RoomParticipant> currentCount = participantRepository.findByRoomToken(room.getRoomToken());

        if (currentCount.size() >= 2) {
            log.warn("Room {} is full (capacity: 2)", room.getRoomToken());
            throw new RuntimeException("This interview room is full. Maximum 2 participants allowed.");
        }
    }


    private void validateRoomStatus(InterviewRoom room) {
        if (room.getStatus() == RoomStatus.EXPIRED) {
            throw new RuntimeException("This interview room has expired");
        }

        if (room.getStatus() == RoomStatus.COMPLETED) {
            throw new RuntimeException("This interview has already been completed");
        }
    }


        private Optional<JoinRoomResponse> handleRejoinScenario(InterviewRoom room, Long userId, String webSocketSession)
        {
        String roomToken = room.getRoomToken();


        log.info("User {} rejoining room {} - returning existing session", userId, room.getRoomToken());

        RoomParticipant existingParticipant = participantRepository
                .findByRoomTokenAndUserId(roomToken, userId)
                .orElseThrow(() -> new RuntimeException("Participant record not found during rejoin"));


        List<ParticipantInfo> otherParticipants = getOtherParticipantsOptimized(room);

        // Build rejoin response
        JoinRoomResponse response = entityMapper.buildJoinResponse(
                room, existingParticipant, getIceServers(), otherParticipants);

        return Optional.of(response);
    }


    private void firstUserJoined(InterviewRoom room, Long userId) {
        if (room.getStatus() != RoomStatus.SCHEDULED) {
            return;
        }
        log.info("First user joining room {} - ", room.getRoomToken());

        ParticipantRole role = determineParticipantRole(room, userId);

        logEvent(room.getRoomToken(), EventType.PARTICIPANT_JOINED, userId,
                role.toString() + " joined");

        room.setStatus(RoomStatus.WAITING);
        roomRepository.save(room);

        log.info("Room {} status changed to WAITING", room.getRoomToken());
    }

    private RoomParticipant createAndAddParticipant(InterviewRoom room, Long userId) {
        // Determine participant role
        ParticipantRole role = determineParticipantRole(room, userId);

        // Create participant entity
        RoomParticipant participant = RoomParticipant.builder()
                .roomToken(room.getRoomToken())
                .userId(userId)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .status(ParticipantStatus.CONNECTED)
                .build();

        participant = participantRepository.save(participant);
        log.debug("Created participant record for user {} in room {}", userId, room.getRoomToken());


        log.info("User {} added to room {} as {}", userId, room.getRoomToken(), role);

        logEvent(room.getRoomToken(), EventType.PARTICIPANT_JOINED, userId,
                role.name() + " joined");

        return participant;
    }


    private void activateRoomIfReady(InterviewRoom room, RoomParticipant newParticipant) {
        long participantCount = participantRepository.countByRoomTokenAndStatus(
                room.getRoomToken(), ParticipantStatus.CONNECTED);

        if (participantCount < 2 || room.getStatus() != RoomStatus.WAITING) {
            log.debug("Room {} not ready to activate (participants: {}, status: {})",
                    room.getRoomToken(), participantCount, room.getStatus());
            return;
        }

        qualityCacheService.initializeRoom(room.getRoomToken());

        room.setStartedAt(LocalDateTime.now());
        room.setStatus(RoomStatus.ACTIVE);
        roomRepository.save(room);

        log.info("Both participants present in room {} - activating and starting billing", room.getRoomToken());

    }



    private void rollbackRoomActivation(
            InterviewRoom room,
            RoomParticipant participant,
            Exception billingError) {

        log.error("CRITICAL: Billing failed for room {} - rolling back",
                room.getRoomToken(), billingError);

        // 1. Revert room status
        room.setStatus(RoomStatus.WAITING);
        room.setStartedAt(null);
        roomRepository.save(room);

        // 2. Remove participant from Redis
        String roomToken = room.getRoomToken();
        String username = participant.getUserId().toString();
//        roomService.leaveRoom(roomToken, username);

        // 3. Delete participant from database
        participantRepository.delete(participant);

        // 4. Log failure event
        logEvent(room.getRoomToken(), EventType.BILLING_FAILED, participant.getUserId(),
                "Failed to start billing: " + billingError.getMessage());

        log.info("Rollback completed for room {}", room.getRoomToken());
    }


    private JoinRoomResponse buildJoinResponse(InterviewRoom room, RoomParticipant participant) {
        // Get other participants (optimized - only if room is ACTIVE)
        List<ParticipantInfo> otherParticipants = getOtherParticipantsOptimized(room);

        // Get ICE servers for WebRTC
        List<String> iceServers = getIceServers();

        // Use mapper to build response
        JoinRoomResponse response = entityMapper.buildJoinResponse(
                room, participant, iceServers, otherParticipants);

        log.info("User {} successfully joined room {} - Status: {}, Billing: {}",
                participant.getUserId(), room.getRoomToken(),
                room.getStatus(), response.getBillingEnabled());

        return response;
    }

    private List<ParticipantInfo> getOtherParticipantsOptimized(InterviewRoom room) {
        if (room.getStatus() != RoomStatus.ACTIVE) {
            return Collections.emptyList();
        }

        List<RoomParticipant> participants = participantRepository
                .findByRoomTokenAndStatus(room.getRoomToken(), ParticipantStatus.CONNECTED);

        return entityMapper.toParticipantInfoList(participants);
    }

    private void validateJoinTiming(InterviewRoom room, Long userId)
    {

        if(Objects.equals(userId, room.getInterviewerId()))
        {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDate scheduledAt = room.getScheduledAt();

        // Calculate how early user is trying to join
        long minutesEarly = ChronoUnit.MINUTES.between(now, scheduledAt);

        if (minutesEarly > 5) {
            throw new RuntimeException(
                    String.format("Interview starts in %d minutes. Please join no more than 5 minutes early.",
                            minutesEarly)
            );
        }

        long minutesLate = ChronoUnit.MINUTES.between(scheduledAt, now);

        if (minutesLate > 15) {
            throw new RuntimeException(
                    "This interview was scheduled to start over 15 minutes ago and is now closed."
            );
        }
    }

    @Transactional
    public void leaveRoom(String roomToken, Long userId) {
        log.info("User {} leaving room {}", userId, roomToken);

//         Update participant status
        participantRepository.findByRoomTokenAndUserId(roomToken, userId)
                .ifPresent(participant -> {
                    participant.setStatus(ParticipantStatus.DISCONNECTED);
                    participant.setLeftAt(LocalDateTime.now());
                    participantRepository.save(participant);
                });

        logEvent(roomToken, EventType.PARTICIPANT_LEFT, userId, null);


        long activeParticipants = participantRepository.countByRoomTokenAndStatus(
                roomToken, ParticipantStatus.CONNECTED);

        if (activeParticipants == 0) {
            endRoom(roomToken);
        }

        Map<String,Object> res = qualityCacheService.getRoomData(roomToken);

        //TODO: CALL BILLING SERVICE AND CALCULATE BILLING AND STORE ALL DETAILS IN DB

        log.info("User {} left room {}", userId, roomToken);
    }

    @Transactional
    public void endRoom(String roomToken) {
        log.info("Ending room: {}", roomToken);

        InterviewRoom room = roomRepository.findByRoomToken(roomToken)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        room.setStatus(RoomStatus.COMPLETED);
        room.setEndedAt(LocalDateTime.now());
        roomRepository.save(room);

        // Log event
        logEvent(roomToken, EventType.CALL_ENDED, null, null);

        log.info("Room {} ended", roomToken);
    }


    @Transactional
    public void forceEndRoom(String roomToken, String reason) {
        log.warn("Force ending room {} - Reason: {}", roomToken, reason);

        try {
            InterviewRoom room = roomRepository.findByRoomToken(roomToken)
                    .orElseThrow(() -> new RoomAccessException("Room not found"));

            forceDisconnectAllParticipants(room);

            room.setStatus(RoomStatus.COMPLETED);
            room.setEndedAt(LocalDateTime.now());
            roomRepository.save(room);

            logEvent(room.getRoomToken(), EventType.ROOM_TERMINATED, null, reason);

            log.info("Room {} force terminated successfully", roomToken);

        } catch (Exception e) {
            log.error("Error force ending room {}", roomToken, e);
            throw new RoomAccessException("Failed to force end room");
        }
    }

    private void forceDisconnectAllParticipants(InterviewRoom room) {
        List<RoomParticipant> participants = participantRepository.findByRoomToken(room.getRoomToken());

        for (RoomParticipant participant : participants) {
            if (participant.getStatus() == ParticipantStatus.CONNECTED) {
                participant.setStatus(ParticipantStatus.DISCONNECTED);
                participant.setLeftAt(LocalDateTime.now());
                participantRepository.save(participant);

                log.info("Force disconnected user {} from room {}",
                        participant.getUserId(), room.getId());
            }
        }
    }


    public RoomResponse getRoomDetails(String roomToken) {

        if (roomToken == null || roomToken.isBlank()) {
            throw new RoomAccessException("Room token must not be null or empty");
        }

        try {
            InterviewRoom room = roomRepository.findByRoomToken(roomToken)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("InterviewRoom", "roomToken", roomToken)
                    );

            List<ParticipantInfo> participants = participantRepository.findByRoomToken(roomToken)
                    .stream()
                    .map(this::mapToParticipantInfo)
                    .collect(Collectors.toList());

            return entityMapper.toRoomResponse(room, participants);

        } catch (ResourceNotFoundException ex) {
            // Known business exception → rethrow
            throw ex;

        } catch (Exception ex) {
            log.error("Error while fetching room details for token: {}", roomToken, ex);
            throw new InternalServerErrorException("Failed to fetch room details. Please try again later.");
        }
    }

    private ParticipantRole determineParticipantRole(InterviewRoom room, Long userId) {
        if (userId.equals(room.getInterviewerId())) {
            return ParticipantRole.INTERVIEWER;
        }
        return ParticipantRole.INTERVIEWEE;
    }

    private List<ParticipantInfo> getOtherParticipants(String roomToken, Long currentUserId) {
        return participantRepository.findByRoomToken(roomToken).stream()
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

    private void logEvent(String roomToken, EventType eventType, Long userId, String metadata) {
        RoomEvent event = RoomEvent.builder()
                .roomToken(roomToken)
                .eventType(eventType)
                .userId(userId)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();

        eventRepository.save(event);
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

    public RoomResponse getRoomByInterviewId(String bookingReference)
    {
        try{
            log.info("Fetching room for interview: {}", bookingReference);

            if (bookingReference == null || bookingReference.isBlank()) {
                throw new BadRequestException("Booking reference must not be null or empty");
            }

            InterviewRoom room = roomRepository.findByBookingReference(bookingReference)
                    .orElseThrow(() -> new RuntimeException("Room not found for interview"));

            List<ParticipantInfo> participants = participantRepository.findByRoomToken(room.getRoomToken())
                    .stream()
                    .map(this::mapToParticipantInfo)
                    .collect(Collectors.toList());

            return entityMapper.toRoomResponse(room, participants);
        } catch (Exception e)
        {
            log.error("Unexpected error while creating room for bookingReference: {}",
                   bookingReference, e);
            throw new InternalServerErrorException("Unable to fetch room by booking reference");
        }

    }


    public List<RoomResponse> getUserRooms(Long userId)
    {
        if (userId == null || userId <= 0) {
            throw new BadRequestException("User ID must be a positive number");
        }

        try{
            log.info("Fetching rooms for user: {}", userId);

            List<InterviewRoom> rooms = roomRepository.findByParticipantUserId(userId);

            return rooms.stream()
                    .map(room -> {
                        List<ParticipantInfo> participants = participantRepository.findByRoomToken(room.getRoomToken())
                                .stream()
                                .map(this::mapToParticipantInfo)
                                .collect(Collectors.toList());
                        return entityMapper.toRoomResponse(room, participants);
                    })
                    .collect(Collectors.toList());
        }
        catch (Exception ex) {
            log.error("Unexpected error while fetching rooms for user: {}", userId, ex);
            throw new InternalServerErrorException("Unexpected error occurred while fetching user rooms");
        }
    }


    public List<RoomResponse> getActiveRooms() {
        log.info("Fetching active rooms");

        List<InterviewRoom> rooms = roomRepository.findActiveRooms();

        return rooms.stream()
                .map(room -> {
                    List<ParticipantInfo> participants = participantRepository.findByRoomToken(room.getRoomToken())
                            .stream()
                            .map(this::mapToParticipantInfo)
                            .collect(Collectors.toList());
                    return entityMapper.toRoomResponse(room, participants);
                })
                .collect(Collectors.toList());
    }


    @Transactional
    public void startRecording(String roomToken, Long userId) {
        log.info("Starting recording for room: {} by user: {}", roomToken, userId);

        InterviewRoom room = roomRepository.findByRoomToken(roomToken)
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
        logEvent(roomToken, EventType.RECORDING_STARTED, userId, null);

        // In production, start actual recording with media server
        // For now, just log the action
        log.info("Recording started for room: {}", roomToken);
    }


    @Transactional
    public String stopRecording(String roomToken, Long userId) {
        log.info("Stopping recording for room: {} by user: {}", roomToken, userId);

        InterviewRoom room = roomRepository.findByRoomToken(roomToken)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Validate user is interviewer
        if (!userId.equals(room.getInterviewerId())) {
            throw new RuntimeException("Only interviewer can stop recording");
        }

        // Generate recording URL (in production, this would come from media server)
        String recordingUrl = "/recordings/" + roomToken + ".mp4";
        room.setRecordingUrl(recordingUrl);
        roomRepository.save(room);

        // Log event
        logEvent(roomToken, EventType.RECORDING_STOPPED, userId, recordingUrl);

        log.info("Recording stopped for room: {}, URL: {}", roomToken, recordingUrl);

        return recordingUrl;
    }


    public List<RoomEventDTO> getRoomEvents(String roomToken) {
        log.info("Fetching events for room: {}", roomToken);

        List<RoomEvent> events = eventRepository.findByRoomTokenOrderByTimestampDesc(roomToken);


        return entityMapper.toRoomEventDTO(events);
    }


    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    @Transactional
    public void cleanupExpiredRooms() {
        log.info("Running cleanup for expired rooms");

        LocalDateTime expiryTime = LocalDateTime.now().minusHours(2);
        List<InterviewRoom> expiredRooms = roomRepository.findExpiredRooms(expiryTime);

        for (InterviewRoom room : expiredRooms) {
            log.info("Cleaning up expired room: {} (status: {})", room.getId(), room.getStatus());

            try {
                if (room.getStatus() == RoomStatus.ACTIVE) {
                    log.warn("Room {} was ACTIVE but expired - attempting to save billing data", room.getId());
                    try {
                        // Try to save whatever quality data exists in Redis
//                        billingService.endBillingSession(room.getRoomToken());
                        log.info("Successfully saved billing data for expired active room: {}", room.getId());
                    } catch (Exception e) {
                        log.error("Failed to save billing data for expired room: {}", room.getId(), e);
                        // Continue cleanup even if billing fails
                    }
                }

                room.setStatus(RoomStatus.EXPIRED);
                room.setEndedAt(LocalDateTime.now());
                roomRepository.save(room);

                List<RoomParticipant> participants = participantRepository.findByRoomToken(room.getRoomToken());
                for (RoomParticipant participant : participants) {
                    if (participant.getStatus() == ParticipantStatus.CONNECTED) {
                        // Update database
                        participant.setStatus(ParticipantStatus.DISCONNECTED);
                        participant.setLeftAt(LocalDateTime.now());
                        participantRepository.save(participant);

                        // Remove from Redis
                        String username = participant.getUserId().toString();
//                        roomService.leaveRoom(room.getId().toString(), username);

                        log.debug("Disconnected participant {} from expired room {}",
                                participant.getUserId(), room.getId());
                    }
                }

                // Clean up room participants (if any left)
//                roomService.cleanupRoom(room.getId().toString());

                // Clean up quality cache
//                qualityCacheService.cleanupRoomCache(room.getId().toString());

                log.info("Cleaned up all Redis data for room {}", room.getId());

//                logEvent(room.getId(), EventType.ROOM_EXPIRED, null,
//                        String.format("Room expired after 2 hours (original status: %s)", room.getStatus()));


            } catch (Exception e) {
                log.error("Error cleaning up expired room {}: {}", room.getId(), e.getMessage(), e);
                // Continue with next room even if this one fails
            }
        }

        log.info("Cleaned up {} expired rooms", expiredRooms.size());
    }


}