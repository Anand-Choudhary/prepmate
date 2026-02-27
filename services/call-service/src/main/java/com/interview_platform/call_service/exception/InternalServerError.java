package com.interview_platform.call_service.exception;

public class InternalServerError extends RuntimeException
{
    public InternalServerError(String message) {
        super(message);
    }

    public InternalServerError(String message, Throwable cause) {
        super(message, cause);
    }

}
