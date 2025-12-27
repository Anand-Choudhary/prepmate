package com.prepmate.producer_service.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SmsNotificationRequest extends NotificationRequest
{
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Message is required")
    private String message;
}
