package com.example.user_service.dto.requestDto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class UserRequestDto
{
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobile;

    @Size(max = 500, message = "Profile image URL must not exceed 500 characters")
    private String profileImg;

    @Size(max = 500, message = "Base image URL must not exceed 500 characters")
    private String baseImg;
}
