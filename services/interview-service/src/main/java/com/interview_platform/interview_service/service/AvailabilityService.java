package com.interview_platform.interview_service.service;

import com.interview_platform.interview_service.dto.*;
import com.interview_platform.interview_service.entity.InterviewSlot;
import com.interview_platform.interview_service.exception.InvalidCancellationException;
import com.interview_platform.interview_service.mapper.EntityMapper;
import com.interview_platform.interview_service.entity.InterviewerAvailability;
import com.interview_platform.interview_service.repository.InterviewSlotRepository;
import com.interview_platform.interview_service.repository.InterviewerAvailabilityRepository;
import com.interview_platform.interview_service.security.UserContext;
import com.interview_platform.interview_service.utils.SlotStatus;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final InterviewerAvailabilityRepository availabilityRepository;
    private final EntityMapper mapper;
    private final SlotGenerationService slotGenerationService;
    private final InterviewSlotRepository slotRepository;

    @Transactional
    public List<SlotResponse> createAvailability(CreateAvailabilityRequest request)
    {
        Long interviewerId = UserContext.getUserId();

        validateAvailabilityRequest(request);

        List<InterviewerAvailability> allAvailabilities = new ArrayList<>();

        // Process each date
        for (DateSlotRequest dateSlot : request.getDateSlots()) {

            String notes = dateSlot.getNotes() != null
                    ? dateSlot.getNotes()
                    : request.getNotes();

            List<InterviewerAvailability> dateAvailabilities = dateSlot.getTimeSlots().stream()
                    .map(timeSlot -> InterviewerAvailability.builder()
                            .interviewerId(interviewerId)
                            .availabilityDate(dateSlot.getDate())
                            .startTime(timeSlot.getStartTime())
                            .endTime(timeSlot.getEndTime())
                            .notes(notes)
//                            .isActive(true)
                            .build())
                    .toList();

            allAvailabilities.addAll(dateAvailabilities);
        }

        List<InterviewerAvailability> saved = availabilityRepository.saveAll(allAvailabilities);

        List<InterviewSlot> generatedSlots = slotGenerationService
                .createSlotsFromAvailability(saved);

        List<InterviewSlot> savedSlots = slotRepository.saveAll(generatedSlots);

        // Save all generated slots
//        List<InterviewSlot> savedSlots = slotRepository.saveAll(generatedSlots);
//        allGeneratedSlots.addAll(savedSlots);

        return savedSlots.stream()
                .map(mapper::toInterviewSlotResponse)
                .collect(Collectors.toList());

    }

    private void validateAvailabilityRequest(CreateAvailabilityRequest request) {
        for (DateSlotRequest dateSlot : request.getDateSlots()) {

            if (dateSlot.getDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "Cannot add availability for past date: " + dateSlot.getDate()
                );
            }

            // Validate each time slot
            dateSlot.getTimeSlots().forEach(slot -> {
                if (slot.getEndTime().isBefore(slot.getStartTime()) ||
                        slot.getEndTime().equals(slot.getStartTime())) {
                    throw new IllegalArgumentException(
                            String.format("Invalid time slot on %s: %s - %s. End time must be after start time",
                                    dateSlot.getDate(), slot.getStartTime(), slot.getEndTime())
                    );
                }
            });

            // Check for overlapping slots within the same date
            checkForOverlappingSlots(dateSlot);
        }
    }

    private void checkForOverlappingSlots(DateSlotRequest dateSlot) {
        List<TimeSlotRequest> slots = dateSlot.getTimeSlots();

        for (int i = 0; i < slots.size(); i++) {
            for (int j = i + 1; j < slots.size(); j++) {
                TimeSlotRequest slot1 = slots.get(i);
                TimeSlotRequest slot2 = slots.get(j);

                if (isOverlapping(slot1.getStartTime(), slot1.getEndTime(),
                        slot2.getStartTime(), slot2.getEndTime())) {
                    throw new IllegalArgumentException(
                            String.format("Time slots overlap on %s: [%s - %s] and [%s - %s]",
                                    dateSlot.getDate(),
                                    slot1.getStartTime(), slot1.getEndTime(),
                                    slot2.getStartTime(), slot2.getEndTime())
                    );
                }
            }
        }
    }

    public List<SlotResponse> getAvailableSlots(
            SlotDateRequest request)
    {
        Long interviewerId = UserContext.getUserId();

        List<InterviewSlot> availability = slotRepository.findAvailableSlotsForInterviewer(
                interviewerId, request.getStartDate(), request.getEndDate()
        );

        return availability.stream()
                .map(mapper::toInterviewSlotResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public Boolean cancelSlot(Long slotId) {
        InterviewerAvailability availability = availabilityRepository
                .findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        Optional<InterviewSlot> available = slotRepository.findById(slotId);
        available
                .ifPresentOrElse(
                        slot -> {
                            if (slot.getStatus() == SlotStatus.BOOKED) {
                                throw new InvalidCancellationException("Slot is already booked and cannot be cancelled");
                            }
                            slot.setStatus(SlotStatus.CANCELLED);
                            slotRepository.save(slot);
                        },
                        () -> {
                            throw new ResourceNotFoundException("Slot not found");
                        }
                );

        return true;
    }


    private boolean isOverlapping(LocalTime start1, LocalTime end1,
                                  LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}