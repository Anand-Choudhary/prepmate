package com.interview_platform.call_service.entity;

import jakarta.persistence.Column;
import lombok.*;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MinuteQuality extends BaseModel {


    @Column(name = "room_token", nullable = false, length = 255)
    private String roomToken;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "minute_number", nullable = false)
    private Integer minuteNumber;

    @Column(name = "average_packet_loss")
    private Double averagePacketLoss;

    @Column(name = "average_jitter")
    private Double averageJitter;

    @Column(name = "average_rtt")
    private Double averageRTT;

    @Column(name = "average_frame_rate")
    private Double averageFrameRate;

    @Column(name = "average_bitrate")
    private Double averageBitrate;

    @Column(name = "video_width")
    private Integer videoWidth;

    @Column(name = "video_height")
    private Integer videoHeight;

    @Column(name = "video_codec", length = 50)
    private String videoCodec;

    @Column(name = "audio_codec", length = 50)
    private String audioCodec;

    @Column(name = "seconds_connected")
    private Integer secondsConnected;

    @Column(name = "seconds_disconnected")
    private Integer secondsDisconnected;

    @Column(name = "quality_rating", length = 20)
    private String qualityRating;

    @Column(name = "is_good_quality")
    private Boolean isGoodQuality;

}