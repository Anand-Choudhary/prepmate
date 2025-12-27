package com.prepmate.producer_service.dto;


import com.prepmate.producer_service.utils.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDto
{
    private String id;
    private NotificationStatus status;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();
}
