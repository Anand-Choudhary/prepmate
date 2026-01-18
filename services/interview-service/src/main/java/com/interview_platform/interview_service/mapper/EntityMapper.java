package com.interview_platform.interview_service.mapper;

import com.interview_platform.interview_service.dto.AvailabilityResponse;
import com.interview_platform.interview_service.dto.SlotResponse;
import com.interview_platform.interview_service.entity.InterviewSlot;
import com.interview_platform.interview_service.entity.InterviewerAvailability;
import com.interview_platform.interview_service.utils.SlotStatus;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper
{
    public AvailabilityResponse toResponse(InterviewerAvailability entity) {
        return AvailabilityResponse.builder()
                .id(entity.getId())
                .interviewerId(entity.getInterviewerId())
                .availabilityDate(entity.getAvailabilityDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
//                .isActive(entity.getIsActive())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public SlotResponse toInterviewSlotResponse(InterviewSlot slot) {
        if (slot == null) {
            return null;
        }

        return SlotResponse.builder()
                .id(slot.getId())
                .interviewerId(slot.getInterviewerId())
                .intervieweeId(slot.getIntervieweeId())
                .description(slot.getDescription())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .durationMinutes(slot.getDurationMinutes())
                .status(SlotStatus.valueOf(slot.getStatus().name()))
                .meetingLink(slot.getMeetingLink())
                .videoRoomId(slot.getVideoRoomId())
                .bookedAt(slot.getBookedAt())
                .cancelledAt(slot.getCancelledAt())
                .createdAt(slot.getCreatedAt())
                .updatedAt(slot.getUpdatedAt())
                .build();
    }

}
