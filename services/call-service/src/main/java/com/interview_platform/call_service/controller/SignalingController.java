package com.interview_platform.call_service.controller;

import com.interview_platform.call_service.dto.ChatMessage;
import com.interview_platform.call_service.dto.RoomEventDTO;
import com.interview_platform.call_service.dto.SignalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

/**
 * WebSocket controller for WebRTC signaling
 * Handles SDP offers/answers and ICE candidates exchange
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class SignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle SDP Offer from caller
     * Client sends: /app/signal/offer
     * Server broadcasts to: /topic/room/{roomId}/offer
     */
    @MessageMapping("/signal/offer")
    public void handleOffer(@Payload SignalMessage message) {
        log.info("Received SDP offer from user {} in room {}",
                message.getFromUserId(), message.getRoomId());

        // Forward offer to specific user or broadcast to room
        if (message.getToUserId() != null) {
            // Send to specific user
            messagingTemplate.convertAndSendToUser(
                    message.getToUserId(),
                    "/queue/signal/offer",
                    message
            );
        } else {
            // Broadcast to all users in room
            messagingTemplate.convertAndSend(
                    "/topic/room/" + message.getRoomId() + "/offer",
                    message
            );
        }

        log.info("Forwarded SDP offer to user {} in room {}",
                message.getToUserId(), message.getRoomId());
    }

    /**
     * Handle SDP Answer from callee
     * Client sends: /app/signal/answer
     * Server broadcasts to: /topic/room/{roomId}/answer
     */
    @MessageMapping("/signal/answer")
    public void handleAnswer(@Payload SignalMessage message) {
        log.info("Received SDP answer from user {} in room {}",
                message.getFromUserId(), message.getRoomId());

        // Forward answer to specific user
        if (message.getToUserId() != null) {
            messagingTemplate.convertAndSendToUser(
                    message.getToUserId(),
                    "/queue/signal/answer",
                    message
            );
        } else {
            // Broadcast to all users in room
            messagingTemplate.convertAndSend(
                    "/topic/room/" + message.getRoomId() + "/answer",
                    message
            );
        }

        log.info("Forwarded SDP answer to user {} in room {}",
                message.getToUserId(), message.getRoomId());
    }

    /**
     * Handle ICE Candidates
     * Client sends: /app/signal/candidate
     * Server broadcasts to: /topic/room/{roomId}/candidate
     */
    @MessageMapping("/signal/candidate")
    public void handleIceCandidate(@Payload SignalMessage message) {
        log.debug("Received ICE candidate from user {} in room {}",
                message.getFromUserId(), message.getRoomId());

        // Forward ICE candidate to specific user or broadcast
        if (message.getToUserId() != null) {
            messagingTemplate.convertAndSendToUser(
                    message.getToUserId(),
                    "/queue/signal/candidate",
                    message
            );
        } else {
            messagingTemplate.convertAndSend(
                    "/topic/room/" + message.getRoomId() + "/candidate",
                    message
            );
        }
    }

    /**
     * Handle chat messages
     * Client sends: /app/chat/message
     * Server broadcasts to: /topic/room/{roomId}/chat
     */
    @MessageMapping("/chat/message")
    public void handleChatMessage(@Payload ChatMessage message) {
        log.info("Received chat message from user {} in room {}",
                message.getUserId(), message.getRoomId());

        message.setTimestamp(LocalDateTime.now());

        // Broadcast to all users in room
        messagingTemplate.convertAndSend(
                "/topic/room/" + message.getRoomId() + "/chat",
                message
        );
    }

    /**
     * Notify room about participant events
     * (joined, left, status changes)
     */
    public void notifyRoomEvent(String roomId, RoomEventDTO event) {
        log.info("Broadcasting room event {} to room {}", event.getEventType(), roomId);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/events",
                event
        );
    }
}
