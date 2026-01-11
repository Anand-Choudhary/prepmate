package com.example.user_service.service;

import com.example.user_service.dto.requestDto.ExperienceRequestDto;
import com.example.user_service.dto.responseDto.ExperienceResponseDto;
import com.example.user_service.entity.Experience;
import com.example.user_service.entity.UserProfile;
import com.example.user_service.exception.ResourceNotFoundException;
import com.example.user_service.mapper.EntityMapper;
import com.example.user_service.repository.ExperienceRepository;
import com.example.user_service.repository.UserProfileRepository;
import com.example.user_service.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final UserProfileRepository userProfileRepository;
    private final EntityMapper entityMapper;

    @Transactional
    public ExperienceResponseDto addExperience(ExperienceRequestDto experienceRequestDTO)
    {
        Long userId = UserContext.getUserId();
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        // Validate endYear logic
        if (Boolean.TRUE.equals(experienceRequestDTO.getCurrentlyWorking())
                && experienceRequestDTO.getEndYear() != null) {
            throw new IllegalArgumentException("End year must be null for current job");
        }
        if (Boolean.FALSE.equals(experienceRequestDTO.getCurrentlyWorking())
                && experienceRequestDTO.getEndYear() == null) {
            throw new IllegalArgumentException("End year is required for past job");
        }

        Experience experience = entityMapper.toEntity(experienceRequestDTO);
        profile.addExperience(experience);

        Experience savedExperience = experienceRepository.save(experience);
        return entityMapper.toExperienceResponseDto(savedExperience);
    }

    @Transactional(readOnly = true)
    public ExperienceResponseDto getExperienceById(Long id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience details not found"));
        return entityMapper.toExperienceResponseDto(experience);
    }

    @Transactional(readOnly = true)
    public List<ExperienceResponseDto> getExperienceByUserId(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        return experienceRepository.findByProfileId(profile.getId()).stream()
                .map(entityMapper::toExperienceResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExperienceResponseDto updateExperience(Long id, ExperienceRequestDto experienceRequestDTO) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience details not found"));

        // Validate endYear logic
        if (Boolean.TRUE.equals(experienceRequestDTO.getCurrentlyWorking())
                && experienceRequestDTO.getEndYear() != null) {
            throw new IllegalArgumentException("End year must be null for current job");
        }
        if (Boolean.FALSE.equals(experienceRequestDTO.getCurrentlyWorking())
                && experienceRequestDTO.getEndYear() == null) {
            throw new IllegalArgumentException("End year is required for past job");
        }

        experience.setCompanyName(experienceRequestDTO.getCompanyName());
        experience.setRole(experienceRequestDTO.getRole());
        experience.setStartYear(experienceRequestDTO.getStartYear());
        experience.setCurrentlyWorking(experienceRequestDTO.getCurrentlyWorking());
        experience.setEndYear(experienceRequestDTO.getEndYear());

        Experience updatedExperience = experienceRepository.save(experience);
        return entityMapper.toExperienceResponseDto(updatedExperience);
    }

    @Transactional
    public void deleteExperience(Long id) {
        Experience experience = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experience details not found"));

        UserProfile profile = experience.getProfile();
        profile.removeExperience(experience);
        experienceRepository.delete(experience);
    }
}