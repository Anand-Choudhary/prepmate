package com.interview_platform.payment_service.repository;

import com.interview_platform.payment_service.entity.PaymentWebhook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
interface PaymentWebhookRepository extends JpaRepository<PaymentWebhook, Long> {

    List<PaymentWebhook> findByProcessed(Boolean processed);

    Page<PaymentWebhook> findByWebhookType(String webhookType, Pageable pageable);

    List<PaymentWebhook> findByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT w FROM PaymentWebhook w WHERE w.processed = false " +
            "ORDER BY w.createdAt ASC")
    List<PaymentWebhook> findUnprocessedWebhooks();
}
