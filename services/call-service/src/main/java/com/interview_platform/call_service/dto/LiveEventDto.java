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
public class LiveEventDto
{
    private String eventType;
    private Long userId;
    private String roomToken;
    private LocalDateTime timestamp;
    private String userName;
    private Object metadata;
}
