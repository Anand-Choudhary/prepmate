package com.interview_platform.call_service.exception;

public class BadRequestException extends RuntimeException
{
    private final String field;
    private final Object rejectedValue;

    public BadRequestException(String message) {
        super(message);
        this.field = null;
        this.rejectedValue = null;
    }

    public BadRequestException(String message, String field, Object rejectedValue) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
    }

    public String getField() {
        return field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }
}
