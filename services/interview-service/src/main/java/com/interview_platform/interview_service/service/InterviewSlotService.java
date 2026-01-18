//package com.interview_platform.interview_service.service;
//
//import com.interview_platform.interview_service.dto.*;
//import com.interview_platform.interview_service.entity.InterviewSlot;
//import com.interview_platform.interview_service.exception.InvalidSlotException;
//import com.interview_platform.interview_service.exception.UnauthorizedException;
//import com.interview_platform.interview_service.external.client.UserServiceClient;
//import com.interview_platform.interview_service.repository.InterviewSlotRepository;
//import com.interview_platform.interview_service.utils.SlotStatus;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class InterviewSlotService {
//
//    private final InterviewSlotRepository slotRepository;
//    private final RedisCacheService cacheService;
//    private final UserServiceClient userServiceClient;
////    private final InterviewEventPublisher eventPublisher;
//
//    @Value("${interview.duration.allowed-durations:30,45,60}")
//    private String allowedDurationsStr;
//
//    @Value("${interview.booking.buffer-minutes:15}")
//    private int bufferMinutes;
//
//    @Transactional
//    public SlotCreatedResponse createSlot(String interviewerId, CreateSlotRequest request) {
//        log.info("Creating slot for interviewer: {}", interviewerId);
//
//        // Validate duration
//        validateDuration(request.getDurationMinutes());
//
//        // Validate time is in future
//        if (request.getStartTime().isBefore(LocalDateTime.now())) {
//            throw new InvalidSlotException("Start time must be in the future");
//        }
//
//        // Calculate end time
//        LocalDateTime endTime = request.getStartTime().plusMinutes(request.getDurationMinutes());
//
//        // Check for overlapping slots
//        List<InterviewSlot> overlappingSlots = slotRepository.findOverlappingSlots(
//                interviewerId,
//                request.getStartTime(),
//                endTime
//        );
//
//        if (!overlappingSlots.isEmpty()) {
//            throw new InvalidSlotException("This time slot overlaps with an existing slot");
//        }
//
//        // Create slot
//        InterviewSlot slot = InterviewSlot.builder()
//                .interviewerId(interviewerId)
//                .title(request.getTitle())
//                .description(request.getDescription())
//                .startTime(request.getStartTime())
//                .endTime(endTime)
//                .durationMinutes(request.getDurationMinutes())
//                .status(SlotStatus.AVAILABLE)
//                .build();
//
//        slot = slotRepository.save(slot);
//
//        // Invalidate cache
//        cacheService.invalidateSlotsCache(interviewerId);
//
//        // Publish event
////        eventPublisher.publishSlotCreated(slot.getId(), interviewerId, slot.getStartTime(), slot.getEndTime());
//
//        log.info("Created slot: {} for interviewer: {}", slot.getId(), interviewerId);
//
//        return SlotCreatedResponse.builder()
//                .id(String.valueOf(slot.getId()))
//                .title(slot.getTitle())
//                .startTime(slot.getStartTime())
//                .endTime(slot.getEndTime())
//                .durationMinutes(slot.getDurationMinutes())
//                .message("Interview slot created successfully!")
//                .build();
//    }
//
//    @Transactional
//    public BulkSlotCreatedResponse createBulkSlots(String interviewerId, BulkCreateSlotRequest request) {
//        log.info("Creating bulk slots for interviewer: {} from {} to {}",
//                interviewerId, request.getStartDate(), request.getEndDate());
//
//        validateDuration(request.getDurationMinutes());
//
//        List<SlotCreatedResponse> createdSlots = new ArrayList<>();
//
//        // Parse time
//        LocalTime startTime = LocalTime.parse(request.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
//        LocalTime endTime = LocalTime.parse(request.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
//
//        // Iterate through dates
//        LocalDate currentDate = request.getStartDate().toLocalDate();
//        LocalDate endDate = request.getEndDate().toLocalDate();
//
//        while (!currentDate.isAfter(endDate)) {
//            // Check if this day is in the allowed days
//            int dayOfWeek = currentDate.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
//
//            if (request.getDaysOfWeek().contains(dayOfWeek)) {
//                // Create slots for this day
//                LocalDateTime slotStart = LocalDateTime.of(currentDate, startTime);
//                LocalDateTime dayEnd = LocalDateTime.of(currentDate, endTime);
//
//                while (slotStart.plusMinutes(request.getDurationMinutes()).isBefore(dayEnd) ||
//                        slotStart.plusMinutes(request.getDurationMinutes()).equals(dayEnd)) {
//
//                    LocalDateTime slotEnd = slotStart.plusMinutes(request.getDurationMinutes());
//
//                    // Only create if in future
//                    if (slotStart.isAfter(LocalDateTime.now())) {
//                        // Check for overlap
//                        List<InterviewSlot> overlapping = slotRepository.findOverlappingSlots(
//                                interviewerId, slotStart, slotEnd
//                        );
//
//                        if (overlapping.isEmpty()) {
//                            InterviewSlot slot = InterviewSlot.builder()
//                                    .interviewerId(interviewerId)
//                                    .title(request.getTitle())
//                                    .description(request.getDescription())
//                                    .startTime(slotStart)
//                                    .endTime(slotEnd)
//                                    .durationMinutes(request.getDurationMinutes())
//                                    .status(SlotStatus.AVAILABLE)
//                                    .build();
//
//                            slot = slotRepository.save(slot);
//
//                            createdSlots.add(SlotCreatedResponse.builder()
//                                    .id(String.valueOf(slot.getId()))
//                                    .title(slot.getTitle())
//                                    .startTime(slot.getStartTime())
//                                    .endTime(slot.getEndTime())
//                                    .durationMinutes(slot.getDurationMinutes())
//                                    .build());
//                        }
//                    }
//
//                    // Move to next slot (with buffer)
//                    slotStart = slotEnd.plusMinutes(bufferMinutes);
//                }
//            }
//
//            currentDate = currentDate.plusDays(1);
//        }
//
//        // Invalidate cache
//        cacheService.invalidateSlotsCache(interviewerId);
//
//        log.info("Created {} bulk slots for interviewer: {}", createdSlots.size(), interviewerId);
//
//        return BulkSlotCreatedResponse.builder()
//                .slots(createdSlots)
//                .totalCreated(createdSlots.size())
//                .message("Successfully created " + createdSlots.size() + " interview slots!")
//                .build();
//    }
//
//    /**
//     * Get available slots (for interviewees to browse)
//     */
//    @Transactional(readOnly = true)
//    public AvailableSlotsResponse getAvailableSlots(String interviewerId, LocalDateTime fromDate) {
//        log.debug("Getting available slots for interviewer: {}", interviewerId);
//
//        // Try cache first
//        List<SlotResponse> cachedSlots = cacheService.getCachedAvailableSlots(interviewerId);
//        if (cachedSlots != null) {
//            return AvailableSlotsResponse.builder()
//                    .slots(cachedSlots)
//                    .totalCount(cachedSlots.size())
//                    .queriedAt(LocalDateTime.now())
//                    .build();
//        }
//
//        // Fetch from database
//        LocalDateTime queryFrom = fromDate != null ? fromDate : LocalDateTime.now();
//
//        List<InterviewSlot> slots;
//        if (interviewerId != null) {
//            slots = slotRepository.findByInterviewerIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
//                    interviewerId,
//                    SlotStatus.AVAILABLE,
//                    queryFrom
//            );
//        } else {
//            slots = slotRepository.findAvailableSlots(
//                    SlotStatus.AVAILABLE,
//                    queryFrom
//            );
//        }
//
//        List<SlotResponse> slotResponses = slots.stream()
//                .map(this::convertToSlotResponse)
//                .toList();
//
//        // Cache the results
//        if (interviewerId != null) {
//            cacheService.cacheAvailableSlots(interviewerId, slotResponses);
//        }
//
//        return AvailableSlotsResponse.builder()
//                .slots(slotResponses)
//                .totalCount(slotResponses.size())
//                .queriedAt(LocalDateTime.now())
//                .build();
//    }
//
//    /**
//     * Get slot by ID
//     */
//    @Transactional(readOnly = true)
//    public SlotResponse getSlot(String slotId) {
//        InterviewSlot slot = slotRepository.findById(slotId)
//                .orElseThrow(() -> new SlotNotFoundException("Slot not found: " + slotId));
//
//        return convertToSlotResponse(slot);
//    }
//
//    /**
//     * Get interviewer's slots
//     */
//    @Transactional(readOnly = true)
//    public List<SlotResponse> getInterviewerSlots(String interviewerId) {
//        List<InterviewSlot> slots = slotRepository.findByInterviewerIdAndStatusInOrderByStartTimeAsc(
//                interviewerId,
//                List.of(SlotStatus.AVAILABLE, SlotStatus.BOOKED)
//        );
//
//        return slots.stream()
//                .map(this::convertToSlotResponse)
//                .toList();
//    }
//
//    /**
//     * Update slot
//     */
//    @Transactional
//    public SlotResponse updateSlot(String slotId, String interviewerId, UpdateSlotRequest request) {
//        InterviewSlot slot = slotRepository.findById(slotId)
//                .orElseThrow(() -> new SlotNotFoundException("Slot not found: " + slotId));
//
//        // Verify ownership
//        if (!slot.getInterviewerId().equals(interviewerId)) {
//            throw new UnauthorizedException("You don't have permission to update this slot");
//        }
//
//        // Can only update available slots
//        if (slot.getStatus() != SlotStatus.AVAILABLE) {
//            throw new InvalidSlotException("Cannot update a slot that is already booked");
//        }
//
//        // Update fields
//        if (request.getTitle() != null) {
//            slot.setTitle(request.getTitle());
//        }
//        if (request.getDescription() != null) {
//            slot.setDescription(request.getDescription());
//        }
//        if (request.getStartTime() != null && request.getDurationMinutes() != null) {
//            validateDuration(request.getDurationMinutes());
//            slot.setStartTime(request.getStartTime());
//            slot.setEndTime(request.getStartTime().plusMinutes(request.getDurationMinutes()));
//            slot.setDurationMinutes(request.getDurationMinutes());
//        }
//
//        slot = slotRepository.save(slot);
//
//        // Invalidate cache
//        cacheService.invalidateSlotsCache(interviewerId);
//
//        return convertToSlotResponse(slot);
//    }
//
//    /**
//     * Delete slot
//     */
//    @Transactional
//    public void deleteSlot(String slotId, String interviewerId) {
//        InterviewSlot slot = slotRepository.findById(slotId)
//                .orElseThrow(() -> new SlotNotFoundException("Slot not found: " + slotId));
//
//        // Verify ownership
//        if (!slot.getInterviewerId().equals(interviewerId)) {
//            throw new UnauthorizedException("You don't have permission to delete this slot");
//        }
//
//        // Can only delete available slots
//        if (slot.getStatus() != SlotStatus.AVAILABLE) {
//            throw new InvalidSlotException("Cannot delete a slot that is already booked");
//        }
//
//        slotRepository.delete(slot);
//
//        // Invalidate cache
//        cacheService.invalidateSlotsCache(interviewerId);
//
//        log.info("Deleted slot: {}", slotId);
//    }
//
//    /**
//     * Convert slot entity to response DTO
//     */
//    private SlotResponse convertToSlotResponse(InterviewSlot slot) {
//        return SlotResponse.builder()
//                .id(slot.getId())
//                .interviewerId(slot.getInterviewerId())
//                .intervieweeId(slot.getIntervieweeId())
//                .title(slot.getTitle())
//                .description(slot.getDescription())
//                .startTime(slot.getStartTime())
//                .endTime(slot.getEndTime())
//                .durationMinutes(slot.getDurationMinutes())
//                .status(slot.getStatus())
//                .meetingLink(slot.getMeetingLink())
//                .videoRoomId(slot.getVideoRoomId())
//                .version(slot.getVersion())
//                .createdAt(slot.getCreatedAt())
//                .bookedAt(slot.getBookedAt())
//                .build();
//    }
//
//    /**
//     * Validate duration
//     */
//    private void validateDuration(int duration) {
//        List<Integer> allowedDurations = Arrays.stream(allowedDurationsStr.split(","))
//                .map(String::trim)
//                .map(Integer::parseInt)
//                .toList();
//
//        if (!allowedDurations.contains(duration)) {
//            throw new InvalidSlotException(
//                    "Invalid duration. Allowed durations: " + allowedDurationsStr + " minutes"
//            );
//        }
//    }
//}
