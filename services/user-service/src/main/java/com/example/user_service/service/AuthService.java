package com.example.user_service.service;



import com.example.user_service.dto.requestDto.LoginRequestDto;
import com.example.user_service.dto.requestDto.RegisterRequestDto;
import com.example.user_service.dto.responseDto.AuthResponseDto;
import com.example.user_service.entity.User;
import com.example.user_service.entity.UserProfile;
import com.example.user_service.exception.UsernameAlreadyExistsException;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate token with additional claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
//        claims.put("role", user.getRole());

        String token = jwtUtil.generateToken(user.getEmail(), claims);

        return new AuthResponseDto(user.getId(), token, user.getEmail(), user.getName());
    }


    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UsernameAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setDob(request.getDateOfBirth());

        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .build();
        savedUser.setProfile(profile);

        return new AuthResponseDto(savedUser.getId(),savedUser.getName(), savedUser.getEmail());
    }
}
