package com.interview_platform.call_service.mapper;

import com.interview_platform.call_service.dto.JoinRoomResponse;
import com.interview_platform.call_service.dto.ParticipantInfo;
import com.interview_platform.call_service.dto.RoomEventDTO;
import com.interview_platform.call_service.dto.RoomResponse;
import com.interview_platform.call_service.entity.InterviewRoom;
import com.interview_platform.call_service.entity.RoomEvent;
import com.interview_platform.call_service.entity.RoomParticipant;
import com.interview_platform.call_service.utils.RoomStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class EntityMapper {

    // Optional: Inject UserService if you need to fetch user details
    // private final UserService userService;

    /**
     * Build JoinRoomResponse from room and participant
     */
    public JoinRoomResponse buildJoinResponse(
            InterviewRoom room,
            RoomParticipant participant,
            List<String> iceServers, List<ParticipantInfo> otherParticipants) {

        log.debug("Building join response for user {} in room {}",
                participant.getUserId(), room.getId());

        return JoinRoomResponse.builder()
                .roomToken(room.getRoomToken())
                .userId(participant.getUserId())
                .role(participant.getRole().name())
                .status(participant.getStatus().name())
                .roomStatus(room.getStatus().name())
                .billingEnabled(room.getStatus() == RoomStatus.ACTIVE)
                .iceServers(iceServers)
                .otherParticipants(Collections.emptyList()) // Will be set by service
                .startedAt(room.getStartedAt())
                .scheduledAt(room.getScheduledAt())
                .build();
    }


    public ParticipantInfo toParticipantInfo(RoomParticipant participant) {
        if (participant == null) {
            return null;
        }

        return ParticipantInfo.builder()
                .userId(participant.getUserId())
                .name(getUserName(participant.getUserId())) // Fetch from User Service
                .role(participant.getRole().name())
                .status(participant.getStatus().name())
                .joinedAt(participant.getJoinedAt())
                .leftAt(participant.getLeftAt())
                .build();
    }

    /**
     * Convert list of RoomParticipants to ParticipantInfos
     */
    public List<ParticipantInfo> toParticipantInfoList(List<RoomParticipant> participants) {
        if (participants == null || participants.isEmpty()) {
            return Collections.emptyList();
        }

        return participants.stream()
                .map(this::toParticipantInfo)
                .collect(Collectors.toList());
    }


    public RoomResponse toRoomResponse(InterviewRoom room,List<ParticipantInfo> participants) {
        if (room == null) {
            return null;
        }

        return RoomResponse.builder()
                .roomToken(room.getRoomToken())
                .bookingReference(room.getBookingReference())
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

    /**
     * Build RoomResponse with participants
     */
//    public RoomResponse toRoomResponse(
//            InterviewRoom room,
//            List<ParticipantInfo> participants) {
//
//        RoomResponse response = toRoomResponse(room);
//        if (response != null) {
//
//        }
//        return response;
//    }

    /**
     * Convert RoomEvent to RoomEventDTO
     */
    public List<RoomEventDTO> toRoomEventDTO(List<RoomEvent> events) {
        if (events == null) {
            return null;
        }
//        return events.stream()
//                .map(event -> RoomEventDTO.builder()


        return events.stream()
                .map(event -> RoomEventDTO.builder()
                        .eventType(event.getEventType().name())
                        .roomToken(event.getRoomToken())
                        .userId(event.getUserId())
                        .userName("User " + event.getUserId()) // TODO: Fetch from User Service
                        .timestamp(event.getTimestamp())
                        .metadata(event.getMetadata())
                        .build())
                .collect(Collectors.toList());
    }


    /**
     * Get user name by ID
     * TODO: Implement actual user service integration
     */
    private String getUserName(Long userId) {
        if (userId == null) {
            return "Unknown User";
        }

        // In production, fetch from User Service:
        // return userService.getUserById(userId)
        //         .map(User::getName)
        //         .orElse("User " + userId);

        // For now, return placeholder
        return "User " + userId;
    }
}
