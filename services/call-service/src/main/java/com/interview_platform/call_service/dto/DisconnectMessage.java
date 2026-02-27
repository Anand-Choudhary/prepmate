package com.interview_platform.call_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisconnectMessage
{
    private String type;
    private String participantToken;
    private java.time.LocalDateTime timestamp;

}
