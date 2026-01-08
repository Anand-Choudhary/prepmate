package com.example.user_service.controller;

import com.example.user_service.dto.requestDto.LoginRequestDto;
import com.example.user_service.dto.responseDto.ApiResponse;
import com.example.user_service.dto.responseDto.AuthResponseDto;
import com.example.user_service.dto.responseDto.SkillResponseDto;
import com.example.user_service.service.SkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    // Global skill endpoints
//    @PostMapping("/skills")
//    public ResponseEntity<SkillResponseDTO> createSkill(@Valid @RequestBody SkillRequestDTO skillRequestDTO) {
//        SkillResponseDTO skill = skillService.createSkill(skillRequestDTO);
//        return new ResponseEntity<>(skill, HttpStatus.CREATED);
//    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<SkillResponseDto>>> getAllSkills() {
        List<SkillResponseDto> skills = skillService.getAllSkills();
        return ResponseEntity.ok(ApiResponse.success("All skills fetched", skills));
    }

    @GetMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<SkillResponseDto>> getSkillById(@PathVariable Long skillId) {
        SkillResponseDto skill = skillService.getSkillById(skillId);
        return ResponseEntity.ok(ApiResponse.success("skill fetched", skill));
    }

    @DeleteMapping("/skills/{skillId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteSkill(@PathVariable Long skillId) {
        skillService.deleteSkill(skillId);
        return ResponseEntity.ok(ApiResponse.success("skill deleted", true));
    }

    // User-specific skill endpoints
    @PostMapping("/users/{userId}/skills/{skillId}")
    public ResponseEntity<ApiResponse<SkillResponseDto>> addSkillToUser(
            @PathVariable Long userId,
            @PathVariable Long skillId) {
        SkillResponseDto skill = skillService.addSkillToUser(userId, skillId);
        return ResponseEntity.ok(ApiResponse.success("skill added to user", skill));
    }

    @PostMapping("/users/{userId}/skills")
    public ResponseEntity<ApiResponse<SkillResponseDto>> addSkillToUserByName(
            @PathVariable Long userId,
            @RequestParam String name) {
        SkillResponseDto skill = skillService.addSkillToUserByName(userId, name);
        return ResponseEntity.ok(ApiResponse.success("skill added to user", skill));
    }

    @GetMapping("/users/{userId}/skills")
    public ResponseEntity<ApiResponse<Set<SkillResponseDto>>> getUserSkills(@PathVariable Long userId) {
        Set<SkillResponseDto> skills = skillService.getUserSkills(userId);
        return ResponseEntity.ok(ApiResponse.success("User skills fetched", skills));
    }

    @DeleteMapping("/users/{userId}/skills/{skillId}")
    public ResponseEntity<ApiResponse<Boolean>> removeSkillFromUser(
            @PathVariable Long userId,
            @PathVariable Long skillId) {
        skillService.removeSkillFromUser(userId, skillId);
        return ResponseEntity.ok(ApiResponse.success("skill removed for the user", true));
    }
}