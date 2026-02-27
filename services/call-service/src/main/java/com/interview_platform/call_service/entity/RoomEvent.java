package com.interview_platform.call_service.entity;


import com.interview_platform.call_service.utils.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "room_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomEvent extends BaseModel
{
    @Column(nullable = false)
    private String roomToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
