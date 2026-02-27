package com.interview_platform.call_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String traceId;

    // For validation errors
    private Map<String, String> fieldErrors;

    // For multiple errors
    private List<String> errors;

    // Additional debug information (only in dev mode)
    private String debugMessage;
}