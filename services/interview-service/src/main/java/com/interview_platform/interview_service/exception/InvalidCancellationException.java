package com.interview_platform.interview_service.exception;

public class InvalidCancellationException extends RuntimeException {
    public InvalidCancellationException(String message) {
        super(message);
    }
}