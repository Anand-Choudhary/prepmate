//package com.interview_platform.interview_service.entity;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.EnumType;
//import jakarta.persistence.Enumerated;
//import lombok.*;
//
//import java.time.DayOfWeek;
//import java.time.LocalTime;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class SlotAvailabilityRule extends BaseModel
//{
//    @Column(name = "interviewer_id", nullable = false)
//    private Long interviewerId;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "day_of_week", nullable = false)
//    private DayOfWeek dayOfWeek;
//
//    @Column(name = "start_time", nullable = false)
//    private LocalTime startTime;
//
//    @Column(name = "end_time", nullable = false)
//    private LocalTime endTime;
//
//    @Column(name = "is_active", nullable = false)
//    private Boolean isActive;
//}
