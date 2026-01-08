package com.example.user_service.controller;

import com.example.user_service.dto.requestDto.LoginRequestDto;
import com.example.user_service.dto.requestDto.ProjectRequestDto;
import com.example.user_service.dto.responseDto.ApiResponse;
import com.example.user_service.dto.responseDto.AuthResponseDto;
import com.example.user_service.dto.responseDto.ProjectResponseDto;
import com.example.user_service.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prep-mate/api/users/{userId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponseDto>> addProject(
            @PathVariable Long userId,
            @Valid @RequestBody ProjectRequestDto projectRequestDTO) {
        ProjectResponseDto project = projectService.addProject(userId, projectRequestDTO);
        return ResponseEntity.ok(ApiResponse.success("Project added", project));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponseDto>>> getUserProjects(@PathVariable Long userId) {
        List<ProjectResponseDto> projects = projectService.getProjectsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Project details fetched", projects));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> getProjectById(@PathVariable Long projectId) {
        ProjectResponseDto project = projectService.getProjectById(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project fetched", project));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequestDto projectRequestDTO) {
        ProjectResponseDto updatedProject = projectService.updateProject(projectId, projectRequestDTO);
        return ResponseEntity.ok(ApiResponse.success("Project updated", updatedProject));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project deleted", true));
    }
}