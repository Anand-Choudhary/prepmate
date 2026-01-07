package com.example.user_service.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequestDto
{
    @NotBlank(message = "Project name is required")
    private String projectName;

    @NotBlank(message = "Project description is required")
    @Size(max = 1000, message = "Project description must not exceed 1000 characters")
    private String projectDescription;

    @NotBlank(message = "Project tech stack is required")
    @Size(max = 500, message = "Project tech stack must not exceed 500 characters")
    private String projectTechStack;
}
