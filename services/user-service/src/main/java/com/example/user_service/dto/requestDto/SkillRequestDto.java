package com.example.user_service.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillRequestDto
{
    @NotBlank(message = "Skill name is required")
    private String name;
}
