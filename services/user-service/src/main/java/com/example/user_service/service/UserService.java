package com.example.user_service.service;

import com.example.user_service.dto.requestDto.LoginRequestDto;
import com.example.user_service.dto.requestDto.UserRequestDto;
import com.example.user_service.dto.responseDto.UserCompleteResponseDto;
import com.example.user_service.dto.responseDto.UserResponseDto;
import com.example.user_service.entity.User;
import com.example.user_service.mapper.EntityMapper;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService
{
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return entityMapper.toUserResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UserCompleteResponseDto getUserWithProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return entityMapper.toUserCompleteResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return entityMapper.toUserResponseDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(entityMapper::toUserResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setName(userRequestDTO.getName());
        user.setDob(userRequestDTO.getDob());
        user.setMobile(userRequestDTO.getMobile());
        user.setProfileImg(userRequestDTO.getProfileImg());
        user.setBaseImg(userRequestDTO.getBaseImg());

        User updatedUser = userRepository.save(user);
        return entityMapper.toUserResponseDto(updatedUser);
    }

}
