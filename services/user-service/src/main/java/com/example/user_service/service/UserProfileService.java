package com.example.user_service.service;

import com.example.user_service.dto.responseDto.UserProfileResponseDto;
import com.example.user_service.entity.UserProfile;
import com.example.user_service.exception.ResourceNotFoundException;
import com.example.user_service.mapper.EntityMapper;
import com.example.user_service.repository.UserProfileRepository;
import com.example.user_service.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserProfile()
    {
        Long userId = UserContext.getUserId();
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        return entityMapper.toUserProfileResponseDto(profile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getProfileById(Long profileId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));
        return entityMapper.toUserProfileResponseDto(profile);
    }
}
