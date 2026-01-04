package com.interview_platform.interview_service.exception;

public class BookingLimitExceededException extends RuntimeException {
    public BookingLimitExceededException(String message) {
        super(message);
    }
}