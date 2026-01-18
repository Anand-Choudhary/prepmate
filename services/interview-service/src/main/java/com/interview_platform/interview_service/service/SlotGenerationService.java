package com.interview_platform.interview_service.service;

import com.interview_platform.interview_service.entity.InterviewSlot;
import com.interview_platform.interview_service.entity.InterviewerAvailability;
import com.interview_platform.interview_service.utils.SlotStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotGenerationService {

    @Value("${interview.slot.duration-minutes:30}")
    private Integer slotDurationMinutes;

    public List<InterviewSlot> createSlotsFromAvailability(List<InterviewerAvailability> availabilities) {

        log.info("Creating slots for {} availabilities", availabilities.size());

        List<InterviewSlot> allSlots = new ArrayList<>();

        for (InterviewerAvailability availability : availabilities) {
            List<InterviewSlot> slots = createSlotsFromSingleAvailability(availability);
            allSlots.addAll(slots);
        }

        log.info("Generated total {} slots from {} availabilities",
                allSlots.size(), availabilities.size());

        return allSlots;
    }

    /**
     * Create slots from a single availability
     */
    public List<InterviewSlot> createSlotsFromSingleAvailability(
            InterviewerAvailability availability) {

        List<InterviewSlot> slots = new ArrayList<>();

        LocalTime currentTime = availability.getStartTime();
        LocalTime endTime = availability.getEndTime();

        log.debug("Creating slots for date: {}, time range: {} - {}",
                availability.getAvailabilityDate(),
                currentTime,
                endTime);

        while (canCreateSlot(currentTime, endTime)) {

            LocalDateTime slotStart = LocalDateTime.of(
                    availability.getAvailabilityDate(),
                    currentTime);

            LocalDateTime slotEnd = slotStart.plusMinutes(slotDurationMinutes);

            InterviewSlot slot = InterviewSlot.builder()
                    .interviewerId(availability.getInterviewerId())
                    .slotDate(availability.getAvailabilityDate())
                    .startTime(slotStart)
                    .endTime(slotEnd)
                    .durationMinutes(slotDurationMinutes)
                    .status(SlotStatus.AVAILABLE)
                    .build();

            slots.add(slot);

            // Move to next slot
            currentTime = currentTime.plusMinutes(slotDurationMinutes);
        }

        log.debug("Created {} slots for date {}", slots.size(), availability.getAvailabilityDate());

        return slots;
    }

    /**
     * Check if we can create another slot
     */
    private boolean canCreateSlot(LocalTime currentTime, LocalTime endTime) {
        LocalTime slotEndTime = currentTime.plusMinutes(slotDurationMinutes);
        return slotEndTime.isBefore(endTime) || slotEndTime.equals(endTime);
    }
}