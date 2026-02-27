package com.interview_platform.call_service.controller;

import com.interview_platform.call_service.dto.*;
import com.interview_platform.call_service.service.VideoCallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/call")
@RequiredArgsConstructor
@Slf4j
public class VideoCallController {

    private final VideoCallService videoCallService;
    private final SignalingController signalingController;


    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody CreateRoomRequest request)
    {
        log.info("Creating room for interview: {}", request.getBookingReference());
        RoomResponse response = videoCallService.createRoom(request);
        return ResponseEntity.ok(ApiResponse.success("Room created for interview", response));
    }


    @PostMapping("/rooms/join")
    public ResponseEntity<ApiResponse<JoinRoomResponse>> joinRoom(@Valid @RequestBody JoinRoomRequest request,
                                                                  @RequestHeader(value = "X-WebSocket-Session-Id", required = false) String sessionId) {

        log.info("User {} attempting to join room", request.getUserId());
        JoinRoomResponse response = videoCallService.joinRoom(request,sessionId);

        LiveEventDto event = LiveEventDto.builder()
                .eventType("USER_JOINED")
                .userId(request.getUserId())
                .roomToken(request.getRoomToken())
                .timestamp(LocalDateTime.now())
                .build();

        signalingController.notifyRoomEvent(request.getRoomToken(), event);
        return ResponseEntity.ok(ApiResponse.success("Room joined for interview", response));
    }



    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomDetails(@PathVariable String roomToken) {

        log.info("Fetching details for room: {}", roomToken);
        RoomResponse response = videoCallService.getRoomDetails(roomToken);
        return ResponseEntity.ok(ApiResponse.success("Room details fetched", response));

    }


    @GetMapping("/rooms/interview/{interviewId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomByInterviewId(@PathVariable String bookingReference) {

        log.info("Fetching room for interview: {}", bookingReference);
        RoomResponse response = videoCallService.getRoomByInterviewId(bookingReference);
        return ResponseEntity.ok(ApiResponse.success("Room details fetched", response));
    }

    @PostMapping("/rooms/leave")
    public ResponseEntity<ApiResponse<Boolean>> leaveRoom(
            @Valid @RequestBody LeaveRoomRequest request) {

        log.info("User {} leaving room {}", request.getRoomToken(), request.getUserId());

        videoCallService.leaveRoom(request.getRoomToken(), request.getUserId());

        LiveEventDto event = LiveEventDto.builder()
                .eventType("USER_LEFT")
                .userId(request.getUserId())
                .roomToken(request.getRoomToken())
                .timestamp(LocalDateTime.now())
                .build();

        signalingController.notifyRoomEvent(request.getRoomToken(), event);
        return ResponseEntity.ok(ApiResponse.success("Left room successfully", Boolean.TRUE));
    }


    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<?>> endRoom(
            @PathVariable String roomToken,
            @RequestParam String userId) {

        log.info("User {} ending room {}", userId, roomToken);
        videoCallService.endRoom(roomToken);
        return ResponseEntity.ok(ApiResponse.success("Left room successfully", Boolean.TRUE));
    }


    @GetMapping("/rooms/user/{userId}")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getUserRooms(@PathVariable Long userId) {

        log.info("Fetching rooms for user: {}", userId);

        List<RoomResponse> rooms = videoCallService.getUserRooms(userId);
        return ResponseEntity.ok(ApiResponse.success("Left room successfully", rooms));
    }


    @GetMapping("/rooms/active")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getActiveRooms() {

        log.info("Fetching active rooms");

        List<RoomResponse> rooms = videoCallService.getActiveRooms();
        return ResponseEntity.ok(ApiResponse.success("Active rooms fetched successfully", rooms));
    }


    @PostMapping("/rooms/{roomId}/recording/start")
    public ResponseEntity<ApiResponse<Boolean>> startRecording(
            @PathVariable String roomToken,
            @RequestParam Long userId) {

        log.info("User {} starting recording for room {}", userId, roomToken);

        videoCallService.startRecording(roomToken, userId);
        return ResponseEntity.ok(ApiResponse.success("Recording Started", Boolean.TRUE));
    }

    @PostMapping("/rooms/{roomId}/recording/stop")
    public ResponseEntity<ApiResponse<String>> stopRecording(
            @PathVariable String roomToken,
            @RequestParam Long userId) {

        log.info("User {} stopping recording for room {}", userId, roomToken);

        String recordingUrl = videoCallService.stopRecording(roomToken, userId);
        return ResponseEntity.ok(ApiResponse.success("Recording stopped", recordingUrl));
    }


    @GetMapping("/rooms/{roomId}/events")
    public ResponseEntity<ApiResponse<List<RoomEventDTO>>> getRoomEvents(@PathVariable String roomToken) {

        log.info("Fetching events for room: {}", roomToken);

        List<RoomEventDTO> events = videoCallService.getRoomEvents(roomToken);
        return ResponseEntity.ok(ApiResponse.success("Room events fetched successfully", events));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Boolean>> health()
    {
        return ResponseEntity.ok(ApiResponse.success("Video Call Service is running", Boolean.TRUE));
    }
}

