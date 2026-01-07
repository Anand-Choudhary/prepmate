package com.example.user_service.dto.responseDto;


import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationResponseDto
{
    private Long id;
    private String degree;
    private String institute;
    private LocalDate startDate;
    private LocalDate endDate;
    private Date createdAt;
    private Date updatedAt;
}
