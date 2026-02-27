package com.interview_platform.call_service.exception;

public class RoomAccessException extends RuntimeException
{
    private final String roomToken;
    private final String userId;

    public RoomAccessException(String message, String roomToken, String userId) {
        super(message);
        this.roomToken = roomToken;
        this.userId = userId;
    }

    public RoomAccessException(String message) {
        super(message);
        this.roomToken = null;
        this.userId = null;
    }

    public String getRoomToken() {
        return roomToken;
    }

    public String getUserId() {
        return userId;
    }
}
