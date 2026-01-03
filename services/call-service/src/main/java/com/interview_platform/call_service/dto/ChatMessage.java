package com.interview_platform.call_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String roomId;
    private String userId;
    private String userName;
    private String message;
    private LocalDateTime timestamp;
}

