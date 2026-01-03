package com.prepmate.consumer_service.dao;

import com.prepmate.consumer_service.entity.NotificationLog;
import com.prepmate.consumer_service.utility.NotificationStatus;
import com.prepmate.consumer_service.utility.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationLog, String>
{
    List<NotificationLog> findByUserId(String userId);

    List<NotificationLog> findByEventType(String eventType);

    List<NotificationLog> findByStatus(String status);

    List<NotificationLog> findBySentAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatusAndChannel(String status, String channel);
}
