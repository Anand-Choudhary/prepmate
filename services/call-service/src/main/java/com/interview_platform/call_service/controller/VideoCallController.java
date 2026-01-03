package com.interview_platform.call_service.controller;

import com.interview_platform.call_service.dto.*;
import com.interview_platform.call_service.service.VideoCallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
@Slf4j
public class VideoCallController {

    private final VideoCallService videoCallService;

    /**
     * Create a new interview room
     * POST /api/video/rooms
     */
    @PostMapping("/rooms")
    public ResponseEntity<RoomResponse> createRoom(
            @Valid @RequestBody CreateRoomRequest request) {

        log.info("Creating room for interview: {}", request.getInterviewId());

        try {
            RoomResponse response = videoCallService.createRoom(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to create room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Join an existing room
     * POST /api/video/rooms/join
     */
    @PostMapping("/rooms/join")
    public ResponseEntity<?> joinRoom(@Valid @RequestBody JoinRoomRequest request) {

        log.info("User {} attempting to join room", request.getUserId());

        try {
            JoinRoomResponse response = videoCallService.joinRoom(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Failed to join room: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error joining room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to join room"));
        }
    }

    /**
     * Get room details by ID
     * GET /api/video/rooms/{roomId}
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<?> getRoomDetails(@PathVariable UUID roomId) {

        log.info("Fetching details for room: {}", roomId);

        try {
            RoomResponse response = videoCallService.getRoomDetails(roomId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Room not found: {}", roomId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Room not found"));
        } catch (Exception e) {
            log.error("Error fetching room details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch room details"));
        }
    }

    /**
     * Get room by interview ID
     * GET /api/video/rooms/interview/{interviewId}
     */
    @GetMapping("/rooms/interview/{interviewId}")
    public ResponseEntity<?> getRoomByInterviewId(@PathVariable String interviewId) {

        log.info("Fetching room for interview: {}", interviewId);

        try {
            RoomResponse response = videoCallService.getRoomByInterviewId(interviewId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Room not found for interview: {}", interviewId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Room not found for interview"));
        } catch (Exception e) {
            log.error("Error fetching room by interview ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch room"));
        }
    }

    /**
     * Leave a room
     * POST /api/video/rooms/{roomId}/leave
     */
    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<?> leaveRoom(
            @PathVariable UUID roomId,
            @RequestParam String userId) {

        log.info("User {} leaving room {}", userId, roomId);

        try {
            videoCallService.leaveRoom(roomId, userId);
            return ResponseEntity.ok(new SuccessResponse("Left room successfully"));
        } catch (Exception e) {
            log.error("Error leaving room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to leave room"));
        }
    }

    /**
     * End a room (terminate call)
     * DELETE /api/video/rooms/{roomId}
     */
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<?> endRoom(
            @PathVariable UUID roomId,
            @RequestParam String userId) {

        log.info("User {} ending room {}", userId, roomId);

        try {
            videoCallService.endRoom(roomId);
            return ResponseEntity.ok(new SuccessResponse("Room ended successfully"));
        } catch (Exception e) {
            log.error("Error ending room", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to end room"));
        }
    }

    /**
     * Get all rooms for a user (as interviewer or interviewee)
     * GET /api/video/rooms/user/{userId}
     */
    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<?> getUserRooms(@PathVariable String userId) {

        log.info("Fetching rooms for user: {}", userId);

        try {
            List<RoomResponse> rooms = videoCallService.getUserRooms(userId);
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("Error fetching user rooms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch rooms"));
        }
    }

    /**
     * Get active rooms
     * GET /api/video/rooms/active
     */
    @GetMapping("/rooms/active")
    public ResponseEntity<?> getActiveRooms() {

        log.info("Fetching active rooms");

        try {
            List<RoomResponse> rooms = videoCallService.getActiveRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            log.error("Error fetching active rooms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch active rooms"));
        }
    }

    /**
     * Start recording
     * POST /api/video/rooms/{roomId}/recording/start
     */
    @PostMapping("/rooms/{roomId}/recording/start")
    public ResponseEntity<?> startRecording(
            @PathVariable UUID roomId,
            @RequestParam String userId) {

        log.info("User {} starting recording for room {}", userId, roomId);

        try {
            videoCallService.startRecording(roomId, userId);
            return ResponseEntity.ok(new SuccessResponse("Recording started"));
        } catch (RuntimeException e) {
            log.error("Failed to start recording: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error starting recording", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to start recording"));
        }
    }

    /**
     * Stop recording
     * POST /api/video/rooms/{roomId}/recording/stop
     */
    @PostMapping("/rooms/{roomId}/recording/stop")
    public ResponseEntity<?> stopRecording(
            @PathVariable UUID roomId,
            @RequestParam String userId) {

        log.info("User {} stopping recording for room {}", userId, roomId);

        try {
            String recordingUrl = videoCallService.stopRecording(roomId, userId);
            return ResponseEntity.ok(new RecordingResponse(recordingUrl));
        } catch (RuntimeException e) {
            log.error("Failed to stop recording: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error stopping recording", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to stop recording"));
        }
    }

    /**
     * Get room events/history
     * GET /api/video/rooms/{roomId}/events
     */
    @GetMapping("/rooms/{roomId}/events")
    public ResponseEntity<?> getRoomEvents(@PathVariable UUID roomId) {

        log.info("Fetching events for room: {}", roomId);

        try {
            List<RoomEventDTO> events = videoCallService.getRoomEvents(roomId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error fetching room events", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch room events"));
        }
    }

    /**
     * Health check
     * GET /api/video/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Video Call Service is running");
    }
}

