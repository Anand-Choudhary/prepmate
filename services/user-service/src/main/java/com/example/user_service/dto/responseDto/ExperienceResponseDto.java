package com.example.user_service.dto.responseDto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceResponseDto
{
    private Long id;
    private String companyName;
    private String role;
    private Integer startYear;
    private Boolean currentlyWorking;
    private Integer endYear;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
