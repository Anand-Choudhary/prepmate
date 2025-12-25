package com.example.user_service.service;

import com.example.user_service.dto.requestDto.LoginRequestDto;

import java.util.Optional;

public class UserService implements BaseService<LoginRequestDto, Long>
{

    @Override
    public Optional<LoginRequestDto> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Iterable<LoginRequestDto> findAll() {
        return null;
    }

    @Override
    public LoginRequestDto create(LoginRequestDto loginRequestDto) {
        return null;
    }

    @Override
    public LoginRequestDto update(LoginRequestDto loginRequestDto) {
        return null;
    }
}
