package com.example.user_service.controller;

import com.example.user_service.dto.requestDto.CertificationRequestDto;
import com.example.user_service.dto.responseDto.ApiResponse;
import com.example.user_service.dto.responseDto.CertificationResponseDto;
import com.example.user_service.service.CertificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;

    // Global certification endpoints
//    @PostMapping("/certifications")
//    public ResponseEntity<CertificationResponseDto> createCertification(
//            @Valid @RequestBody CertificationRequestDto certificationRequestDTO) {
//        CertificationResponseDto certification = certificationService.createCertification(certificationRequestDTO);
//        return new ResponseEntity<>(certification, HttpStatus.CREATED);
//    }

    @GetMapping("/certifications")
    public ResponseEntity<ApiResponse<List<CertificationResponseDto>>> getAllCertifications() {
        List<CertificationResponseDto> certifications = certificationService.getAllCertifications();
        return ResponseEntity.ok(ApiResponse.success("All certification fetched", certifications));
    }

    @GetMapping("/certifications/{certificationId}")
    public ResponseEntity<ApiResponse<CertificationResponseDto>> getCertificationById(@PathVariable Long certificationId) {
        CertificationResponseDto certification = certificationService.getCertificationById(certificationId);
        return ResponseEntity.ok(ApiResponse.success("Certification fetched", certification));
    }

    @PutMapping("/certifications/{certificationId}")
    public ResponseEntity<ApiResponse<CertificationResponseDto>> updateCertification(
            @PathVariable Long certificationId,
            @Valid @RequestBody CertificationRequestDto certificationRequestDTO) {
        CertificationResponseDto updatedCertification = certificationService.updateCertification(certificationId, certificationRequestDTO);
        return ResponseEntity.ok(ApiResponse.success("Certification updated", updatedCertification));
    }

    @DeleteMapping("/certifications/{certificationId}")
    public ResponseEntity<ApiResponse<Boolean>> deleteCertification(@PathVariable Long certificationId) {
        certificationService.deleteCertification(certificationId);
        return ResponseEntity.ok(ApiResponse.success("Certification deleted", true));
    }

    // User-specific certification endpoints
    @PostMapping("/users/{userId}/certifications/{certificationId}")
    public ResponseEntity<ApiResponse<CertificationResponseDto>> addCertificationToUser(
            @PathVariable Long userId,
            @PathVariable Long certificationId) {
        CertificationResponseDto certification = certificationService.addCertificationToUser(userId, certificationId);
        return ResponseEntity.ok(ApiResponse.success("Certification added to user", certification));
    }

    @PostMapping("/users/{userId}/certifications")
    public ResponseEntity<ApiResponse<CertificationResponseDto>> addCertificationToUserByName(
            @PathVariable Long userId,
            @Valid @RequestBody CertificationRequestDto certificationRequestDTO) {
        CertificationResponseDto certification = certificationService.addCertificationToUserByName(userId, certificationRequestDTO);
        return ResponseEntity.ok(ApiResponse.success("Certification added to user", certification));
    }

    @GetMapping("/users/{userId}/certifications")
    public ResponseEntity<ApiResponse<Set<CertificationResponseDto>>> getUserCertifications(@PathVariable Long userId) {
        Set<CertificationResponseDto> certifications = certificationService.getUserCertifications(userId);
        return ResponseEntity.ok(ApiResponse.success("Fetch certification for the user", certifications));
    }

    @DeleteMapping("/users/{userId}/certifications/{certificationId}")
    public ResponseEntity<ApiResponse<Boolean>> removeCertificationFromUser(
            @PathVariable Long userId,
            @PathVariable Long certificationId) {
        certificationService.removeCertificationFromUser(userId, certificationId);
        return ResponseEntity.ok(ApiResponse.success("Certification removed for user", true));
    }
}