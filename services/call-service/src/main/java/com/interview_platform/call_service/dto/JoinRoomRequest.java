package com.interview_platform.call_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomRequest {
    @NotBlank(message = "Room token is required")
    private String roomToken;

    @NotBlank(message = "User ID is required")
    private String userId;
}
