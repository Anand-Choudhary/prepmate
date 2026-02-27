package com.interview_platform.call_service.entity;

import com.interview_platform.call_service.utils.ParticipantRole;
import com.interview_platform.call_service.utils.ParticipantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "room_participants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomParticipant extends BaseModel {

    @Column(nullable = false)
    private String roomToken;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;

    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status = ParticipantStatus.WAITING;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    private Integer participantGoodMinutes;

    private Integer participantPoorMinutes;

    private Double participantDiscount;

}