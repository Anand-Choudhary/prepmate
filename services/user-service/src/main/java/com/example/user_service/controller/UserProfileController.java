package com.example.user_service.controller;

import com.example.user_service.dto.responseDto.ApiResponse;
import com.example.user_service.dto.responseDto.UserProfileResponseDto;
import com.example.user_service.service.UserProfileService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getUserProfile() {
        UserProfileResponseDto profile = userProfileService.getUserProfile();
        return ResponseEntity.ok(ApiResponse.success("User profile fetched", profile));
    }
}