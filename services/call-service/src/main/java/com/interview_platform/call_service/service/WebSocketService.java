package com.interview_platform.call_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview_platform.call_service.dto.DisconnectMessage;
import com.interview_platform.call_service.dto.RoomWarningMessage;
import com.interview_platform.call_service.dto.TerminationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
//    private final ObjectMapper objectMapper;

    public void sendToParticipant(String participantToken, Object payload) {
        try {
            String destination = "/topic/participant/" + participantToken;
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("Message sent to participant: {} at destination: {}", participantToken, destination);
        } catch (Exception e) {
            log.error("Failed to send message to participant: {}", participantToken, e);
            throw e;
        }
    }


    public void sendTerminationMessage(String participantToken, String reason) {
        try {
            TerminationMessage message = TerminationMessage.builder()
                    .type("ROOM_TERMINATION")
                    .reason(reason)
                    .message("The call has ended. You will be disconnected shortly.")
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            sendToParticipant(participantToken, message);
            log.info("Termination message sent to participant: {}", participantToken);
        } catch (Exception e) {
            log.error("Failed to send termination message to participant: {}", participantToken, e);
        }
    }


    public void forceDisconnect(String participantToken) {
        try {
            DisconnectMessage message = DisconnectMessage.builder()
                    .type("FORCE_DISCONNECT")
                    .participantToken(participantToken)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            sendToParticipant(participantToken, message);
            log.info("Force disconnect signal sent to participant: {}", participantToken);


        } catch (Exception e) {
            log.error("Failed to force disconnect participant: {}", participantToken, e);
        }
    }


    public void broadcastToRoom(String roomToken, Object payload) {
        try {
            String destination = "/topic/room/" + roomToken;
            messagingTemplate.convertAndSend(destination, payload);
            log.debug("Message broadcast to room: {}", roomToken);
        } catch (Exception e) {
            log.error("Failed to broadcast to room: {}", roomToken, e);
            throw e;
        }
    }


    public void sendRoomWarning(String roomToken, String warningMessage) {
        try {
            RoomWarningMessage message = RoomWarningMessage.builder()
                    .type("ROOM_WARNING")
                    .message(warningMessage)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            broadcastToRoom(roomToken, message);
            log.info("Warning sent to all participants in room: {}", roomToken);
        } catch (Exception e) {
            log.error("Failed to send room warning: {}", roomToken, e);
        }
    }


}