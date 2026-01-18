package com.interview_platform.interview_service.controller;

import com.interview_platform.interview_service.dto.*;
import com.interview_platform.interview_service.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@Slf4j
public class InterviewSlotController {

    private final AvailabilityService availabilityService;

    @PostMapping("/add-slots")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> createAvailability(
            @Valid @RequestBody CreateAvailabilityRequest request) {
        List<SlotResponse> responses = availabilityService.createAvailability(request);
        return ResponseEntity.ok(ApiResponse.success("Slot created successfully", responses));
    }

    @GetMapping("/get-slots")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getSlotsForInterviewer(
            @Valid @ModelAttribute SlotDateRequest request) {
        List<SlotResponse> responses = availabilityService.getAvailableSlots(request);
        return ResponseEntity.ok(ApiResponse.success("All slots fetched", responses));
    }

    @DeleteMapping("/del/{id}")
    public ResponseEntity<ApiResponse<Boolean>> cancelAvailableSlot(
            @PathVariable Long id) {
        Boolean response = availabilityService.cancelSlot(id);
        return ResponseEntity.ok(ApiResponse.success("Slot deleted", Boolean.TRUE));
    }

}
