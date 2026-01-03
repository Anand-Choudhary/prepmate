package com.interview_platform.call_service.entity;

import com.interview_platform.call_service.utils.ParticipantRole;
import com.interview_platform.call_service.utils.ParticipantStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomParticipant extends BaseModel
{
    @Column(nullable = false)
    private UUID roomId;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;

    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status = ParticipantStatus.WAITING;

    private String connectionQuality;  // GOOD, FAIR, POOR

    @Column(columnDefinition = "TEXT")
    private String metadata;
}
