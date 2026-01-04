package com.interview_platform.interview_service.repository;

import com.interview_platform.interview_service.entity.SlotAvailabilityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface SlotAvailabilityRuleRepository extends JpaRepository<SlotAvailabilityRule, String> {

    // Find rules by interviewer
    List<SlotAvailabilityRule> findByInterviewerIdAndIsActive(String interviewerId, Boolean isActive);

    // Find rules by interviewer and day
    List<SlotAvailabilityRule> findByInterviewerIdAndDayOfWeekAndIsActive(
            String interviewerId,
            DayOfWeek dayOfWeek,
            Boolean isActive
    );
}
