package com.example.user_service.service;

import com.example.user_service.dto.requestDto.EducationRequestDto;
import com.example.user_service.dto.responseDto.EducationResponseDto;
import com.example.user_service.entity.Education;
import com.example.user_service.entity.UserProfile;
import com.example.user_service.exception.ResourceNotFoundException;
import com.example.user_service.mapper.EntityMapper;
import com.example.user_service.repository.EducationRepository;
import com.example.user_service.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationService {

    private final EducationRepository educationRepository;
    private final UserProfileRepository userProfileRepository;
    private final EntityMapper entityMapper;

    @Transactional
    public EducationResponseDto addEducation(Long userId, EducationRequestDto educationRequestDTO) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        Education education = entityMapper.toEntity(educationRequestDTO);
        profile.addEducation(education);

        Education savedEducation = educationRepository.save(education);
        return entityMapper.toEducationResponseDto(savedEducation);
    }

    @Transactional(readOnly = true)
    public EducationResponseDto getEducationById(Long id) {
        Education education = educationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Education details not found"));;
        return entityMapper.toEducationResponseDto(education);
    }

    @Transactional(readOnly = true)
    public List<EducationResponseDto> getEducationByUserId(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        return educationRepository.findByProfileId(profile.getId()).stream()
                .map(entityMapper::toEducationResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public EducationResponseDto updateEducation(Long id, EducationRequestDto educationRequestDTO) {
        Education education = educationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Education details not found"));

        education.setDegree(educationRequestDTO.getDegree());
        education.setInstitute(educationRequestDTO.getInstitute());
        education.setStartDate(educationRequestDTO.getStartDate());
        education.setEndDate(educationRequestDTO.getEndDate());

        Education updatedEducation = educationRepository.save(education);
        return entityMapper.toEducationResponseDto(updatedEducation);
    }

    @Transactional
    public void deleteEducation(Long id) {
        Education education = educationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Education details not found"));

        UserProfile profile = education.getProfile();
        profile.removeEducation(education);
        educationRepository.delete(education);
    }
}