package com.example.user_service.controller;

import com.example.user_service.dto.responseDto.ApiResponse;
import com.example.user_service.service.EducationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prep-mate/api/user-education")
@RequiredArgsConstructor
public class EducationController
{
    private final EducationService educationService;

    @PostMapping("/")
    public ResponseEntity<ApiResponse<EducationDto>> login(@Valid @RequestBody EducationDto request) {
        EducationDto response = educationService.addEducation(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
