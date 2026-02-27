package com.example.user_service.dto.responseDto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationResponseDto
{
    private Long id;
    private String name;
    private String issuedBy;
    private Integer year;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
