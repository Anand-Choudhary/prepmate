package com.example.user_service.mapper;


import com.example.user_service.dto.requestDto.*;
import com.example.user_service.dto.responseDto.*;
import com.example.user_service.entity.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EntityMapper {

    // User mappings
    public User toEntity(UserRequestDto dto) {
        return User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .dob(dto.getDob())
                .mobile(dto.getMobile())
                .profileImg(dto.getProfileImg())
                .baseImg(dto.getBaseImg())
                .build();
    }

    public UserResponseDto toUserResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .dob(user.getDob())
                .mobile(user.getMobile())
                .profileImg(user.getProfileImg())
                .baseImg(user.getBaseImg())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserCompleteResponseDto toUserCompleteResponseDto(User user) {
        UserCompleteResponseDto dto = UserCompleteResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .dob(user.getDob())
                .mobile(user.getMobile())
                .profileImg(user.getProfileImg())
                .baseImg(user.getBaseImg())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        if (user.getProfile() != null) {
            dto.setProfile(toUserProfileResponseDto(user.getProfile()));
        }

        return dto;
    }

    // UserProfile mappings
    public UserProfileResponseDto toUserProfileResponseDto(UserProfile profile) {
        UserProfileResponseDto dto = UserProfileResponseDto.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();

        if (profile.getEducation() != null) {
            dto.setEducation(profile.getEducation().stream()
                    .map(this::toEducationResponseDto)
                    .collect(Collectors.toList()));
        }

        if (profile.getProjects() != null) {
            dto.setProjects(profile.getProjects().stream()
                    .map(this::toProjectResponseDto)
                    .collect(Collectors.toList()));
        }

        if (profile.getSkills() != null) {
            dto.setSkills(profile.getSkills().stream()
                    .map(this::toSkillResponseDto)
                    .collect(Collectors.toSet()));
        }

        if (profile.getExperience() != null) {
            dto.setExperience(profile.getExperience().stream()
                    .map(this::toExperienceResponseDto)
                    .collect(Collectors.toList()));
        }

        if (profile.getCertifications() != null) {
            dto.setCertifications(profile.getCertifications().stream()
                    .map(this::toCertificationResponseDto)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    // Education mappings
    public Education toEntity(EducationRequestDto dto) {
        return Education.builder()
                .degree(dto.getDegree())
                .institute(dto.getInstitute())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }

    public EducationResponseDto toEducationResponseDto(Education education) {
        return EducationResponseDto.builder()
                .id(education.getId())
                .degree(education.getDegree())
                .institute(education.getInstitute())
                .startDate(education.getStartDate())
                .endDate(education.getEndDate())
                .createdAt(education.getCreatedAt())
                .updatedAt(education.getUpdatedAt())
                .build();
    }

    // Experience mappings
    public Experience toEntity(ExperienceRequestDto dto) {
        return Experience.builder()
                .companyName(dto.getCompanyName())
                .role(dto.getRole())
                .startYear(dto.getStartYear())
                .currentlyWorking(dto.getCurrentlyWorking())
                .endYear(dto.getEndYear())
                .build();
    }

    public ExperienceResponseDto toExperienceResponseDto(Experience experience) {
        return ExperienceResponseDto.builder()
                .id(experience.getId())
                .companyName(experience.getCompanyName())
                .role(experience.getRole())
                .startYear(experience.getStartYear())
                .currentlyWorking(experience.getCurrentlyWorking())
                .endYear(experience.getEndYear())
                .createdAt(experience.getCreatedAt())
                .updatedAt(experience.getUpdatedAt())
                .build();
    }

    // Project mappings
    public Projects toEntity(ProjectRequestDto dto) {
        return Projects.builder()
                .projectName(dto.getProjectName())
                .projectDescription(dto.getProjectDescription())
                .projectTechStack(dto.getProjectTechStack())
                .build();
    }

    public ProjectResponseDto toProjectResponseDto(Projects project) {
        return ProjectResponseDto.builder()
                .id(project.getId())
                .projectName(project.getProjectName())
                .projectDescription(project.getProjectDescription())
                .projectTechStack(project.getProjectTechStack())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    // Skill mappings
    public Skills toEntity(SkillRequestDto dto) {
        return Skills.builder()
                .name(dto.getName())
                .build();
    }

    public SkillResponseDto toSkillResponseDto(Skills skill) {
        return SkillResponseDto.builder()
                .id(skill.getId())
                .name(skill.getName())
                .createdAt(skill.getCreatedAt())
                .updatedAt(skill.getUpdatedAt())
                .build();
    }

    // Certification mappings
    public Certification toEntity(CertificationRequestDto dto) {
        return Certification.builder()
                .name(dto.getName())
                .issuedBy(dto.getIssuedBy())
                .year(dto.getYear())
                .build();
    }

    public CertificationResponseDto toCertificationResponseDto(Certification certification) {
        return CertificationResponseDto.builder()
                .id(certification.getId())
                .name(certification.getName())
                .issuedBy(certification.getIssuedBy())
                .year(certification.getYear())
                .createdAt(certification.getCreatedAt())
                .updatedAt(certification.getUpdatedAt())
                .build();
    }
}