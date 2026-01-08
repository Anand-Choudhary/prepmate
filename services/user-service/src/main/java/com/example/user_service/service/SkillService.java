package com.example.user_service.service;

import com.example.user_service.dto.requestDto.SkillRequestDto;
import com.example.user_service.dto.responseDto.SkillResponseDto;
import com.example.user_service.entity.Skills;
import com.example.user_service.entity.UserProfile;
import com.example.user_service.exception.ResourceNotFoundException;
import com.example.user_service.mapper.EntityMapper;
import com.example.user_service.repository.SkillsRepository;
import com.example.user_service.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillsRepository skillsRepository;
    private final UserProfileRepository userProfileRepository;
    private final EntityMapper entityMapper;

//    @Transactional
//    public SkillResponseDto createSkill(SkillRequestDto skillRequestDTO) {
//        if (skillsRepository.existsByName(skillRequestDTO.getName())) {
//            throw new DuplicateResourceException("Skill", "name", skillRequestDTO.getName());
//        }
//
//        Skills skill = entityMapper.toEntity(skillRequestDTO);
//        Skills savedSkill = skillsRepository.save(skill);
//        return entityMapper.toSkillResponseDto(savedSkill);
//    }

    @Transactional
    public SkillResponseDto addSkillToUser(Long userId, Long skillId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        Skills skill = skillsRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        profile.addSkill(skill);
        userProfileRepository.save(profile);

        return entityMapper.toSkillResponseDto(skill);
    }

    @Transactional
    public SkillResponseDto addSkillToUserByName(Long userId, String skillName) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        Skills skill = skillsRepository.findByName(skillName)
                .orElseGet(() -> {
                    Skills newSkill = Skills.builder().name(skillName).build();
                    return skillsRepository.save(newSkill);
                });

        profile.addSkill(skill);
        userProfileRepository.save(profile);

        return entityMapper.toSkillResponseDto(skill);
    }

    @Transactional(readOnly = true)
    public Set<SkillResponseDto> getUserSkills(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        return profile.getSkills().stream()
                .map(entityMapper::toSkillResponseDto)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<SkillResponseDto> getAllSkills() {
        return skillsRepository.findAll().stream()
                .map(entityMapper::toSkillResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SkillResponseDto getSkillById(Long id) {
        Skills skill = skillsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        return entityMapper.toSkillResponseDto(skill);
    }

    @Transactional
    public void removeSkillFromUser(Long userId, Long skillId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        Skills skill = skillsRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        profile.removeSkill(skill);
        userProfileRepository.save(profile);
    }

    @Transactional
    public void deleteSkill(Long id) {
        if (!skillsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Skill not found");
        }
        skillsRepository.deleteById(id);
    }
}