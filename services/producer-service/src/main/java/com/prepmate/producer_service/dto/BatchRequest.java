package com.prepmate.producer_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchRequest
{
    private java.util.List<NotificationRequest> notifications;
}
