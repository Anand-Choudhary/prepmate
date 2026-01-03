package com.interview_platform.call_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class IceCandidate {
    private String candidate;
    private String sdpMid;
    private Integer sdpMLineIndex;
}