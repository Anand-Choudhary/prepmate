package com.example.user_service.controller;

import com.example.user_service.dto.requestDto.LoginRequestDto;
import com.example.user_service.dto.requestDto.RegisterRequestDto;
import com.example.user_service.dto.responseDto.ApiResponse;
import com.example.user_service.dto.responseDto.AuthResponseDto;
import com.example.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prep-mate/api/user")
@RequiredArgsConstructor
public class UserController
{
    private final AuthService authService;

    @PostMapping("/")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(@Valid @RequestBody RegisterRequestDto request) {
        AuthResponseDto response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Account registered", response));
    }

//    @GetMapping("/health")
//    public ResponseEntity<String> health() {
//        return ResponseEntity.ok("Auth Service is running");
//    }

}

