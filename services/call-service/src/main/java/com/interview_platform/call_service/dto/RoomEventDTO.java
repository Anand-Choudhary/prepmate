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
public class RoomEventDTO {
    private String eventType;
    private String roomToken;
    private Long userId;
    private String userName;
    private LocalDateTime timestamp;
    private Object metadata;
}
