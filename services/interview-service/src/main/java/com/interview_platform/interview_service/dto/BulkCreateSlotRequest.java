//package com.interview_platform.interview_service.dto;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotEmpty;
//import jakarta.validation.constraints.NotNull;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class BulkCreateSlotRequest {
//    @NotBlank(message = "Title is required")
//    private String title;
//
//    private String description;
//
//    @NotNull(message = "Start date is required")
//    private LocalDateTime startDate;
//
//    @NotNull(message = "End date is required")
//    private LocalDateTime endDate;
//
//    @NotNull(message = "Duration is required")
//    private Integer durationMinutes;
//
//    @NotEmpty(message = "Days of week cannot be empty")
//    private List<Integer> daysOfWeek;
//
//    @NotNull(message = "Start time is required")
//    private String startTime;
//
//    @NotNull(message = "End time is required")
//    private String endTime;
//}
