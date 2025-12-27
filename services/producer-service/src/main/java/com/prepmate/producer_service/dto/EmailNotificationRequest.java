package com.prepmate.producer_service.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationRequest extends NotificationRequest
{
    @NotBlank(message = "Email is required")
    private String[] to;

    private String[] cc;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    private boolean isHtml = false;
    private Map<String, byte[]> attachments;
}
