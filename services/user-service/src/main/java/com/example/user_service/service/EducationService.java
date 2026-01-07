package com.example.user_service.service;


import com.example.user_service.dto.requestDto.LoginRequestDto;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EducationService
{
    private final UserRepository userRepository;

    public LoginRequestDto create(LoginRequestDto loginRequestDto) {
        return null;
    }
}
