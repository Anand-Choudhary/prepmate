package com.interview_platform.interview_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableSlotsResponse {
    private List<SlotResponse> slots;
    private int totalCount;
    private LocalDateTime queriedAt;
}
