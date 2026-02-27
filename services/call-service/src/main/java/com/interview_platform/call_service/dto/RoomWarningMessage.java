package com.interview_platform.call_service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomWarningMessage
{

    private String type;
    private String message;
    private java.time.LocalDateTime timestamp;

}
