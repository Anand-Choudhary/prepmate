package com.interview_platform.call_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalMessage {
    private String type;
    private String roomId;
    private String fromUserId;
    private String toUserId;
    private Object payload;
}