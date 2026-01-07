package com.example.user_service.dto.responseDto;

import lombok.*;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponseDto
{
    private Long id;
    private Long userId;

    @Builder.Default
    private List<EducationResponseDto> education = new ArrayList<>();

    @Builder.Default
    private List<ProjectResponseDto> projects = new ArrayList<>();

    @Builder.Default
    private Set<SkillResponseDto> skills = new HashSet<>();

    @Builder.Default
    private List<ExperienceResponseDto> experience = new ArrayList<>();

    @Builder.Default
    private Set<CertificationResponseDto> certifications = new HashSet<>();

    private Date createdAt;
    private Date updatedAt;
}
