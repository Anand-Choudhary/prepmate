package com.interview_platform.interview_service.security;

public final class UserContext
{
    private static final ThreadLocal<Long> USERID = new ThreadLocal<>();

    private UserContext() {}

    public static void setUserId(Long userId) {
        USERID.set(userId);
    }

    public static Long getUserId() {
        return USERID.get();
    }

    public static void clear() {
        USERID.remove();
    }
}
