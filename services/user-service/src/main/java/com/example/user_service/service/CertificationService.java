package com.example.user_service.service;

import com.example.user_service.dto.requestDto.CertificationRequestDto;
import com.example.user_service.dto.responseDto.CertificationResponseDto;
import com.example.user_service.entity.Certification;
import com.example.user_service.entity.UserProfile;
import com.example.user_service.exception.DuplicateResourceException;
import com.example.user_service.exception.ResourceNotFoundException;
import com.example.user_service.mapper.EntityMapper;
import com.example.user_service.repository.CertificationRepository;
import com.example.user_service.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final UserProfileRepository userProfileRepository;
    private final EntityMapper entityMapper;

//    @Transactional
//    public CertificationResponseDto createCertification(CertificationRequestDto certificationRequestDTO) {
//        if (certificationRepository.existsByName(certificationRequestDTO.getName())) {
//            throw new DuplicateResourceException("Certification", "name", certificationRequestDTO.getName());
//        }
//
//        Certification certification = entityMapper.toEntity(certificationRequestDTO);
//        Certification savedCertification = certificationRepository.save(certification);
//        return entityMapper.toCertificationResponseDto(savedCertification);
//    }

    @Transactional
    public CertificationResponseDto addCertificationToUser(Long userId, Long certificationId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification details not found"));

        profile.addCertification(certification);
        userProfileRepository.save(profile);

        return entityMapper.toCertificationResponseDto(certification);
    }

    @Transactional
    public CertificationResponseDto addCertificationToUserByName(Long userId, CertificationRequestDto certificationRequestDTO) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        Certification certification = certificationRepository.findByName(certificationRequestDTO.getName())
                .orElseGet(() -> {
                    Certification newCertification = entityMapper.toEntity(certificationRequestDTO);
                    return certificationRepository.save(newCertification);
                });

        profile.addCertification(certification);
        userProfileRepository.save(profile);

        return entityMapper.toCertificationResponseDto(certification);
    }

    @Transactional(readOnly = true)
    public Set<CertificationResponseDto> getUserCertifications(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        return profile.getCertifications().stream()
                .map(entityMapper::toCertificationResponseDto)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public List<CertificationResponseDto> getAllCertifications() {
        return certificationRepository.findAll().stream()
                .map(entityMapper::toCertificationResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CertificationResponseDto getCertificationById(Long id) {
        Certification certification = certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification not found"));
        return entityMapper.toCertificationResponseDto(certification);
    }

    @Transactional
    public CertificationResponseDto updateCertification(Long id, CertificationRequestDto certificationRequestDTO) {
        Certification certification = certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certification details not found"));

        if (!certification.getName().equals(certificationRequestDTO.getName())
                && certificationRepository.existsByName(certificationRequestDTO.getName())) {
            throw new DuplicateResourceException("Duplicate certification found: " + certificationRequestDTO.getName());
        }

        certification.setName(certificationRequestDTO.getName());
        certification.setIssuedBy(certificationRequestDTO.getIssuedBy());
        certification.setYear(certificationRequestDTO.getYear());

        Certification updatedCertification = certificationRepository.save(certification);
        return entityMapper.toCertificationResponseDto(updatedCertification);
    }

    @Transactional
    public void removeCertificationFromUser(Long userId, Long certificationId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        Certification certification = certificationRepository.findById(certificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Certification details not found"));

        profile.removeCertification(certification);
        userProfileRepository.save(profile);
    }

    @Transactional
    public void deleteCertification(Long id) {
        if (!certificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Certification details not found");
        }
        certificationRepository.deleteById(id);
    }
}