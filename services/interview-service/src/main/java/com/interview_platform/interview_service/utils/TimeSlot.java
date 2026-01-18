package com.interview_platform.interview_service.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class TimeSlot
{
    private LocalTime startTime;
    private LocalTime endTime;
}
