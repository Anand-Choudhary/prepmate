package com.example.user_service.controller;

import com.example.user_service.dto.requestDto.EducationRequestDto;
import com.example.user_service.dto.requestDto.ExperienceRequestDto;
import com.example.user_service.dto.responseDto.ApiResponse;
import com.example.user_service.dto.responseDto.EducationResponseDto;
import com.example.user_service.dto.responseDto.ExperienceResponseDto;
import com.example.user_service.service.ExperienceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/prep-mate/api/users/{userId}/experience")
@RequiredArgsConstructor
public class ExperienceController {

    private final ExperienceService experienceService;


    @PostMapping("/")
    public ResponseEntity<ApiResponse<ExperienceResponseDto>> addExperience(
            @PathVariable Long userId,
            @Valid @RequestBody ExperienceRequestDto experienceRequestDTO) {
        ExperienceResponseDto experience = experienceService.addExperience(userId, experienceRequestDTO);
        return ResponseEntity.ok(ApiResponse.success("Experience details added", experience));
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<ExperienceResponseDto>>> getUserExperience(@PathVariable Long userId) {
        List<ExperienceResponseDto> experience = experienceService.getExperienceByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Experience details fetched", experience));
    }

    @GetMapping("/{experienceId}")
    public ResponseEntity<ApiResponse<ExperienceResponseDto>> getExperienceById(@PathVariable Long experienceId) {
        ExperienceResponseDto experience = experienceService.getExperienceById(experienceId);
        return ResponseEntity.ok(ApiResponse.success("Experience details fetched", experience));
    }

    @PutMapping("/{experienceId}")
    public ResponseEntity<ApiResponse<ExperienceResponseDto>> updateExperience(
            @PathVariable Long experienceId,
            @Valid @RequestBody ExperienceRequestDto experienceRequestDTO) {
        ExperienceResponseDto updatedExperience = experienceService.updateExperience(experienceId, experienceRequestDTO);
        return ResponseEntity.ok(ApiResponse.success("Experience details updated", updatedExperience));
    }

//    @DeleteMapping("/{experienceId}")
//    public ResponseEntity<Void> deleteExperience(@PathVariable Long experienceId) {
//        experienceService.deleteExperience(experienceId);
//        return ResponseEntity.noContent().build();
//    }
}