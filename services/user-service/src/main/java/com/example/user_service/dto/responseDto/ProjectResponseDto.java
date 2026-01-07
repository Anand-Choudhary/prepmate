package com.example.user_service.dto.responseDto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponseDto
{
    private Long id;
    private String projectName;
    private String projectDescription;
    private String projectTechStack;
    private Date createdAt;
    private Date updatedAt;
}
