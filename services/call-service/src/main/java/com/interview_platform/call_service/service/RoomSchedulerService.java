package com.interview_platform.call_service.service;

import com.interview_platform.call_service.dto.TerminationMessage;
import com.interview_platform.call_service.entity.InterviewRoom;
import com.interview_platform.call_service.repository.InterviewRoomRepository;
import com.interview_platform.call_service.repository.RoomParticipantRepository;
import com.interview_platform.call_service.utils.RoomStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class RoomSchedulerService {

    private final InterviewRoomRepository roomRepository;
    private final RoomParticipantRepository roomParticipantRepository;
    private final WebSocketService webSocketService;
    private final VideoCallService videoCallService;
    private static final int GRACE_PERIOD_MINUTES = 2;
    private static final int WARNING_1_MINUTES_BEFORE = 1;
    private static final int WARNING_2_SECONDS_BEFORE = 30;



    @Scheduled(fixedRate = 30000)
    @Transactional
    public void checkAndTerminateCalls() {
        try {
            LocalDateTime now = LocalDateTime.now();

            List<InterviewRoom> activeRooms = roomRepository.findByStatus(RoomStatus.ACTIVE);

            if (activeRooms.isEmpty()) {
                return;
            }

            log.debug("Checking {} active rooms for auto-termination", activeRooms.size());

            for (InterviewRoom room : activeRooms) {
                try {
                    processRoom(room, now);
                } catch (Exception e) {
                    log.error("Error processing room {} for auto-termination",
                            room.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("Error in call auto-termination scheduler", e);
        }
    }

    private void processRoom(InterviewRoom room, LocalDateTime now) {
        if (room.getStartedAt() == null) {
            log.warn("Room {} is ACTIVE but has no startedAt timestamp", room.getId());
            return;
        }

        Duration elapsed = Duration.between(room.getStartedAt(), now);
        long elapsedMinutes = elapsed.toMinutes();
        long elapsedSeconds = elapsed.getSeconds();

        int scheduledMinutes = room.getScheduledDurationMinutes();
        int maxAllowedMinutes = scheduledMinutes + GRACE_PERIOD_MINUTES;

        log.trace("Room {} - Elapsed: {} min, Scheduled: {} min, Max: {} min",
                room.getId(), elapsedMinutes, scheduledMinutes, maxAllowedMinutes);

        if (elapsedMinutes == scheduledMinutes - WARNING_1_MINUTES_BEFORE) {
            sendWarning1MinuteLeft(room);
        }

        else if (elapsedMinutes == scheduledMinutes) {
            sendScheduledTimeReached(room);
        }

        // SCENARIO 3: Warning 30 seconds before force termination
        else if (elapsedSeconds >= (maxAllowedMinutes * 60 - WARNING_2_SECONDS_BEFORE) &&
                elapsedSeconds < maxAllowedMinutes * 60) {
            sendWarning30SecondsLeft(room);
        }

        // SCENARIO 4: Force terminate (exceeded max allowed time)
        else if (elapsedMinutes >= maxAllowedMinutes) {
            forceTerminateCall(room, elapsedMinutes);
        }
    }

    private void sendWarning1MinuteLeft(InterviewRoom room) {
        log.info("Room {} - Sending 1 minute warning", room.getId());

        String terminationMessage = "Your scheduled interview time ends in 1 minute.";

        TerminationMessage roomMessage = TerminationMessage.builder()
                .type("WARNING_1_MINUTE")
                .reason("Scheduled time ends")
                .message(terminationMessage)
                .timestamp(LocalDateTime.now())
                .build();


        webSocketService.broadcastToRoom(room.getRoomToken(), roomMessage);
    }

    private void sendScheduledTimeReached(InterviewRoom room) {
        log.info("Room {} - Scheduled time reached, grace period started", room.getId());

        String terminationMessage = "Scheduled interview time has ended. You have " +
                GRACE_PERIOD_MINUTES + " minutes of grace period to wrap up.";

        TerminationMessage roomMessage = TerminationMessage.builder()
                .type("SCHEDULED_TIME_REACHED")
                .reason("Scheduled time ends")
                .message(terminationMessage)
                .timestamp(LocalDateTime.now())
                .build();

        webSocketService.broadcastToRoom(room.getRoomToken(), roomMessage);
    }


    private void sendWarning30SecondsLeft(InterviewRoom room) {
        log.warn("Room {} - Sending 30 seconds final warning", room.getId());

        String terminationMessage = "Call will be terminated in 30 seconds!";

        TerminationMessage roomMessage = TerminationMessage.builder()
                .type("FINAL_WARNING")
                .reason("Scheduled time ends")
                .message(terminationMessage)
                .timestamp(LocalDateTime.now())
                .build();

        webSocketService.broadcastToRoom(room.getRoomToken(), roomMessage);
    }


    @Transactional
    private void forceTerminateCall(InterviewRoom room, long elapsedMinutes) {
        log.warn("⚠️ FORCE TERMINATING room {} - Exceeded time limit (elapsed: {} min, max: {} min)",
                room.getId(), elapsedMinutes,
                room.getScheduledDurationMinutes() + GRACE_PERIOD_MINUTES);

        try {
            // STEP 1: Send force termination notification to frontend
            sendForceTerminationNotification(room);

            // STEP 2: Wait a moment for notification to be delivered
            Thread.sleep(1000);

            // STEP 3: Disconnect all participants (via WebSocket)
            forceDisconnectParticipants(room);

            // STEP 4: End the room (triggers billing, cleanup, etc.)
            videoCallService.forceEndRoom(room.getRoomToken(),
                    "Auto-terminated - Exceeded maximum duration");

            log.info("Successfully force terminated room {}", room.getId());

        } catch (Exception e) {
            log.error("Error force terminating room {}", room.getId(), e);
        }
    }


    private void sendForceTerminationNotification(InterviewRoom room)
    {
        String terminationMessage = "Interview has been automatically terminated due to time limit";
        TerminationMessage roomMessage = TerminationMessage.builder()
                .type("FORCE_TERMINATION")
                .reason("Scheduled time ends")
                .message(terminationMessage)
                .timestamp(LocalDateTime.now())
                .build();

        webSocketService.broadcastToRoom(room.getRoomToken(), roomMessage);
        log.info("Sent force termination notification to room {}", room.getId());
    }

    private void forceDisconnectParticipants(InterviewRoom room)
    {
        String terminationMessage = "You have been disconnected from the interview";
        TerminationMessage roomMessage = TerminationMessage.builder()
                .type("FORCE_DISCONNECT")
                .reason("Scheduled time ends")
                .message(terminationMessage)
                .timestamp(LocalDateTime.now())
                .build();
        webSocketService.broadcastToRoom(room.getRoomToken(), roomMessage);
        log.info("Sent force disconnect command to all participants in room {}", room.getId());
    }

}