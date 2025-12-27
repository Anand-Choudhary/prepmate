package com.prepmate.producer_service.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.prepmate.producer_service.utils.NotificationType;
import com.prepmate.producer_service.utils.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EmailNotificationRequest.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = SmsNotificationRequest.class, name = "SMS"),
        @JsonSubTypes.Type(value = PushNotificationRequest.class, name = "PUSH"),
        @JsonSubTypes.Type(value = WebhookNotificationRequest.class, name = "WEBHOOK")
})
public abstract class NotificationRequest
{
    private String id = UUID.randomUUID().toString();

    @NotNull(message = "Type is required")
    private NotificationType type;

    @NotBlank(message = "User ID is required")
    private String userId;

    private String templateId;
    private Map<String, Object> templateData;
    private Priority priority = Priority.NORMAL;
    private LocalDateTime scheduledAt;
    private Map<String, String> metadata;
}
