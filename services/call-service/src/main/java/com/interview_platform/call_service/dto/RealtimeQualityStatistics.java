package com.interview_platform.call_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RealtimeQualityStatistics
{
    private Integer goodMinutes;
    private Integer poorMinutes;
    private List<Object> recentAlerts;

    public double getQualityPercentage() {
        int total = goodMinutes + poorMinutes;
        return total > 0 ? ((double) goodMinutes / total) * 100 : 0;
    }

}
