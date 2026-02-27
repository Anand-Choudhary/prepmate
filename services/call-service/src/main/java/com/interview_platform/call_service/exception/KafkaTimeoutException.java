package com.interview_platform.call_service.exception;

public class KafkaTimeoutException extends RuntimeException {
    public KafkaTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
