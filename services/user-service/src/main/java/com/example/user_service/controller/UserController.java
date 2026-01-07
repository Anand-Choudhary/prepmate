package com.example.user_service.controller;

import com.example.user_service.dto.requestDto.LoginRequestDto;
import com.example.user_service.dto.requestDto.RegisterRequestDto;
import com.example.user_service.dto.requestDto.UserRequestDto;
import com.example.user_service.dto.responseDto.ApiResponse;
import com.example.user_service.dto.responseDto.AuthResponseDto;
import com.example.user_service.dto.responseDto.UserCompleteResponseDto;
import com.example.user_service.dto.responseDto.UserResponseDto;
import com.example.user_service.service.AuthService;
import com.example.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prep-mate/api/user")
@RequiredArgsConstructor
public class UserController
{
    private final AuthService authService;
    private UserService userService;

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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        UserResponseDto response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User details", response));
    }

    @GetMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<UserCompleteResponseDto>> getUserWithProfile(@PathVariable Long id) {
        UserCompleteResponseDto userProfile = userService.getUserWithProfile(id);
        return ResponseEntity.ok(ApiResponse.success("User profile", userProfile));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByEmail(@PathVariable String email) {
        UserResponseDto response = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("User details", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        List<UserResponseDto> allUsers = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("All users", allUsers));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequestDto userRequestDTO) {
        UserResponseDto updatedUser = userService.updateUser(id, userRequestDTO);
        return ResponseEntity.ok(ApiResponse.success("User data updated", updatedUser));
    }

}

