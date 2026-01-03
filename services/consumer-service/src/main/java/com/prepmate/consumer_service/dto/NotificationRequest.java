package com.prepmate.consumer_service.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.prepmate.consumer_service.utility.NotificationType;
import com.prepmate.consumer_service.utility.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = EmailNotificationRequest.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = SmsNotificationRequest.class, name = "SMS"),
})
public class NotificationRequest
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
