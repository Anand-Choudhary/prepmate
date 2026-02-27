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
public class ParticipantInfo {
    private Long userId;
    private String name;
    private String role;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
}
