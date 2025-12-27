package com.prepmate.producer_service.entities;


import com.prepmate.producer_service.utils.NotificationStatus;
import com.prepmate.producer_service.utils.NotificationType;
import com.prepmate.producer_service.utils.Priority;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity
{
    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(nullable = false, length = 500)
    private String recipient;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String templateId;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private Integer retryCount = 0;

    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String metadata;
}
