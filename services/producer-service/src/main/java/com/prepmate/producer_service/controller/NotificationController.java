package com.prepmate.producer_service.controller;


import com.prepmate.producer_service.dto.BatchRequest;
import com.prepmate.producer_service.dto.NotificationRequest;
import com.prepmate.producer_service.dto.NotificationResponse;
import com.prepmate.producer_service.service.NotificationProducerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController
{
    private final NotificationProducerService producerService;

    @PostMapping("/send")
    public CompletableFuture<ResponseEntity<NotificationResponse>> sendNotification(
            @Valid @RequestBody NotificationRequest request) {

        log.info("Received notification request: {} for user: {}",
                request.getId(), request.getUserId());

        return producerService.sendNotification(request)
                .thenApply(response -> ResponseEntity.status(HttpStatus.ACCEPTED).body(response));
    }

    @PostMapping("/send/batch")
    public CompletableFuture<ResponseEntity<List<NotificationResponse>>> sendBatchNotifications(
            @Valid @RequestBody BatchRequest batchRequest) {

        log.info("Received batch notification request with {} notifications",
                batchRequest.getNotifications().size());

        List<CompletableFuture<NotificationResponse>> futures =
                producerService.sendBatchNotifications(batchRequest.getNotifications());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList()))
                .thenApply(responses -> ResponseEntity.status(HttpStatus.ACCEPTED).body(responses));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Producer Service is running");
    }

}
