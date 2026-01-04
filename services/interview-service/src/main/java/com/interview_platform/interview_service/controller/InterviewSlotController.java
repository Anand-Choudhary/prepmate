package com.interview_platform.interview_service.controller;

import com.interview_platform.interview_service.dto.*;
import com.interview_platform.interview_service.service.InterviewSlotService;
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
@RequestMapping("/api/interviews/slots")
@RequiredArgsConstructor
@Slf4j
public class InterviewSlotController {

    private final InterviewSlotService slotService;

    /**
     * Create a single interview slot (Interviewer)
     */
    @PostMapping
    public ResponseEntity<SlotCreatedResponse> createSlot(
            @RequestHeader("X-User-Id") String interviewerId,
            @Valid @RequestBody CreateSlotRequest request
    ) {
        log.info("Creating slot for interviewer: {}", interviewerId);
        SlotCreatedResponse response = slotService.createSlot(interviewerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Create multiple slots in bulk (Interviewer)
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkSlotCreatedResponse> createBulkSlots(
            @RequestHeader("X-User-Id") String interviewerId,
            @Valid @RequestBody BulkCreateSlotRequest request
    ) {
        log.info("Creating bulk slots for interviewer: {}", interviewerId);
        BulkSlotCreatedResponse response = slotService.createBulkSlots(interviewerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get available slots - can filter by interviewer (Interviewee)
     */
    @GetMapping("/available")
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @RequestParam(required = false) String interviewerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate
    ) {
        log.info("Getting available slots for interviewer: {}", interviewerId);
        AvailableSlotsResponse response = slotService.getAvailableSlots(interviewerId, fromDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get slot by ID
     */
    @GetMapping("/{slotId}")
    public ResponseEntity<SlotResponse> getSlot(@PathVariable String slotId) {
        log.info("Getting slot: {}", slotId);
        SlotResponse response = slotService.getSlot(slotId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get interviewer's own slots (Interviewer)
     */
    @GetMapping("/my-slots")
    public ResponseEntity<List<SlotResponse>> getMySlots(
            @RequestHeader("X-User-Id") String interviewerId
    ) {
        log.info("Getting slots for interviewer: {}", interviewerId);
        List<SlotResponse> slots = slotService.getInterviewerSlots(interviewerId);
        return ResponseEntity.ok(slots);
    }

    /**
     * Update slot (Interviewer)
     */
    @PutMapping("/{slotId}")
    public ResponseEntity<SlotResponse> updateSlot(
            @PathVariable String slotId,
            @RequestHeader("X-User-Id") String interviewerId,
            @Valid @RequestBody UpdateSlotRequest request
    ) {
        log.info("Updating slot: {} by interviewer: {}", slotId, interviewerId);
        SlotResponse response = slotService.updateSlot(slotId, interviewerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete slot (Interviewer)
     */
    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteSlot(
            @PathVariable String slotId,
            @RequestHeader("X-User-Id") String interviewerId
    ) {
        log.info("Deleting slot: {} by interviewer: {}", slotId, interviewerId);
        slotService.deleteSlot(slotId, interviewerId);
        return ResponseEntity.noContent().build();
    }
}
