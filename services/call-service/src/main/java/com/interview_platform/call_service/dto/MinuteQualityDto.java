package com.interview_platform.call_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MinuteQualityDto {

    @NotBlank(message = "Room token is required")
    @Size(max = 255, message = "Room token too long")
    private String roomToken;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotNull(message = "Minute number is required")
    @Min(value = 1, message = "Minute number must be at least 1")
    private Integer minuteNumber;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime minuteStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime minuteEndTime;

    @NotNull(message = "Quality flag is required")
    private Boolean isGoodQuality;

    @NotBlank(message = "Quality rating is required")
    @Pattern(regexp = "EXCELLENT|GOOD|FAIR|POOR", message = "Invalid quality rating")
    private String qualityRating;


    @DecimalMin(value = "0.0", message = "Packet loss cannot be negative")
    @DecimalMax(value = "1.0", message = "Packet loss cannot exceed 100%")
    private Double averagePacketLoss;

    @DecimalMin(value = "0.0", message = "Jitter cannot be negative")
    private Double averageJitter;


    @DecimalMin(value = "0.0", message = "RTT cannot be negative")
    private Double averageRTT;

    @DecimalMin(value = "0.0", message = "Frame rate cannot be negative")
    private Double averageFrameRate;

    @DecimalMin(value = "0.0", message = "Bitrate cannot be negative")
    private Double averageBitrate;

    private Integer videoWidth;
    private Integer videoHeight;
    private String videoCodec;
    private String audioCodec;

    private String issueType;
    private String issueDescription;


    @Min(value = 0, message = "Connected seconds cannot be negative")
    @Max(value = 60, message = "Connected seconds cannot exceed 60")
    private Integer secondsConnected;

    @Min(value = 0, message = "Disconnected seconds cannot be negative")
    @Max(value = 60, message = "Disconnected seconds cannot exceed 60")
    private Integer secondsDisconnected;


    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();


//    public boolean isAcceptableQuality() {
//        return "EXCELLENT".equals(qualityRating) || "GOOD".equals(qualityRating) || "FAIR".equals(qualityRating);
//    }


//    public int getQualityScore() {
//        return switch (qualityRating) {
//            case "EXCELLENT" -> 4;
//            case "GOOD" -> 3;
//            case "FAIR" -> 2;
//            case "POOR" -> 1;
//            default -> 0;
//        };
//    }


//    public boolean wasMostlyConnected() {
//        return secondsConnected != null && secondsConnected >= 45; // At least 75% connected
//    }
}