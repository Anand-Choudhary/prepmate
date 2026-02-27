package com.interview_platform.call_service.utils;

public enum EventType
{
    ROOM_CREATED,
    ROOM_TERMINATED,
    PARTICIPANT_JOINED,
    PARTICIPANT_LEFT,
    CALL_STARTED,
    CALL_ENDED,
    RECORDING_STARTED,
    RECORDING_STOPPED,
    SCREEN_SHARE_STARTED,
    SCREEN_SHARE_STOPPED,
    CHAT_MESSAGE,
    BILLING_FAILED,


    CONNECTION_LOST,
    CONNECTION_RESTORED,
    ROOM_CLOSED,

    /**
     * When billing session starts (room becomes active)
     */
    ROOM_STARTED,

    /**
     * When a quality issue is detected in a minute
     */
    QUALITY_ISSUE_DETECTED,

    /**
     * When minute quality is logged
     */
    MINUTE_QUALITY_LOGGED,

    /**
     * When billing is calculated at end of call
     */
    BILLING_CALCULATED,

    /**
     * When discount is applied due to quality
     */
    QUALITY_DISCOUNT_APPLIED,

    /**
     * When network quality degrades
     */
    NETWORK_DEGRADATION,

    /**
     * When network quality improves
     */
    NETWORK_IMPROVEMENT,

    /**
     * When packet loss exceeds threshold
     */
    HIGH_PACKET_LOSS,

    /**
     * When latency exceeds threshold
     */
    HIGH_LATENCY,

    /**
     * When jitter exceeds threshold
     */
    HIGH_JITTER,

    /**
     * When frame rate drops below threshold
     */
    LOW_FRAMERATE,

    /**
     * When bandwidth is insufficient
     */
    INSUFFICIENT_BANDWIDTH,

    /**
     * When a participant experiences connection issues
     */
    PARTICIPANT_CONNECTION_ISSUE,

    /**
     * When invoice is generated
     */
    INVOICE_GENERATED,

    /**
     * When payment is processed (if applicable)
     */
    PAYMENT_PROCESSED;
}
