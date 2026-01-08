package com.example.user_service.controller;

import com.example.user_service.dto.requestDto.EducationRequestDto;
import com.example.user_service.dto.responseDto.ApiResponse;
import com.example.user_service.dto.responseDto.EducationResponseDto;
import com.example.user_service.service.EducationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prep-mate/api/users/{userId}/education")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;

    @PostMapping("/")
    public ResponseEntity<ApiResponse<EducationResponseDto>> addEducation(
            @PathVariable Long userId,
            @Valid @RequestBody EducationRequestDto educationRequestDTO) {
        EducationResponseDto education = educationService.addEducation(userId, educationRequestDTO);
        return ResponseEntity.ok(ApiResponse.success("Education details added", education));
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<EducationResponseDto>>> getUserEducation(@PathVariable Long userId) {
        List<EducationResponseDto> education = educationService.getEducationByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Education details fetched", education));
    }

    @GetMapping("/{educationId}")
    public ResponseEntity<ApiResponse<EducationResponseDto>> getEducationById(@PathVariable Long educationId) {
        EducationResponseDto education = educationService.getEducationById(educationId);
        return ResponseEntity.ok(ApiResponse.success("Education details fetched", education));
    }

    @PutMapping("/{educationId}")
    public ResponseEntity<ApiResponse<EducationResponseDto>> updateEducation(
            @PathVariable Long educationId,
            @Valid @RequestBody EducationRequestDto educationRequestDTO) {
        EducationResponseDto updatedEducation = educationService.updateEducation(educationId, educationRequestDTO);
        return ResponseEntity.ok(ApiResponse.success("Education details updated", updatedEducation));
    }

    @DeleteMapping("/{educationId}")
    public ResponseEntity<ApiResponse<String>> deleteEducation(@PathVariable Long educationId) {
        educationService.deleteEducation(educationId);
        return ResponseEntity.ok(ApiResponse.success("Education details deleted","Record deleted"));
    }
}
