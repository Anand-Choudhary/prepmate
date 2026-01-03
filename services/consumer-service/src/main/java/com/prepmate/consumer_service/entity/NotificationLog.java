package com.prepmate.consumer_service.entity;

import com.prepmate.consumer_service.utility.NotificationStatus;
import com.prepmate.consumer_service.utility.NotificationType;
import com.prepmate.consumer_service.utility.Priority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String channel;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime sentAt;
}
