package com.prepmate.producer_service.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookNotificationRequest extends NotificationRequest
{
    @NotBlank(message = "URL is required")
    private String url;

    private String method = "POST";
    private Map<String, String> headers;
    private Object payload;
}
