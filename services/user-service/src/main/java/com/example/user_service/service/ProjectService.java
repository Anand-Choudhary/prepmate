package com.example.user_service.service;

import com.example.user_service.dto.requestDto.ProjectRequestDto;
import com.example.user_service.dto.responseDto.ProjectResponseDto;
import com.example.user_service.entity.Projects;
import com.example.user_service.entity.UserProfile;
import com.example.user_service.exception.ResourceNotFoundException;
import com.example.user_service.mapper.EntityMapper;
import com.example.user_service.repository.ProjectsRepository;
import com.example.user_service.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectsRepository projectsRepository;
    private final UserProfileRepository userProfileRepository;
    private final EntityMapper entityMapper;

    @Transactional
    public ProjectResponseDto addProject(Long userId, ProjectRequestDto projectRequestDTO) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        Projects project = entityMapper.toEntity(projectRequestDTO);
        profile.addProject(project);

        Projects savedProject = projectsRepository.save(project);
        return entityMapper.toProjectResponseDto(savedProject);
    }

    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectById(Long id) {
        Projects project = projectsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return entityMapper.toProjectResponseDto(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getProjectsByUserId(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        return projectsRepository.findByProfileId(profile.getId()).stream()
                .map(entityMapper::toProjectResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponseDto updateProject(Long id, ProjectRequestDto projectRequestDTO) {
        Projects project = projectsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        project.setProjectName(projectRequestDTO.getProjectName());
        project.setProjectDescription(projectRequestDTO.getProjectDescription());
        project.setProjectTechStack(projectRequestDTO.getProjectTechStack());

        Projects updatedProject = projectsRepository.save(project);
        return entityMapper.toProjectResponseDto(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {
        Projects project = projectsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        UserProfile profile = project.getProfile();
        profile.removeProject(project);
        projectsRepository.delete(project);
    }
}