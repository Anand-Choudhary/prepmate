package com.interview_platform.interview_service.repository;

import com.interview_platform.interview_service.entity.InterviewerAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface InterviewerAvailabilityRepository extends JpaRepository<InterviewerAvailability, Long> {

    // Get all slots for an interviewer on a specific date
    List<InterviewerAvailability> findByInterviewerIdAndAvailabilityDateAndIsActive(
            Long interviewerId,
            LocalDate date,
            Boolean isActive
    );

    // Get all available slots for an interviewer in a date range
    @Query("SELECT ia FROM InterviewerAvailability ia " +
            "WHERE ia.interviewerId = :interviewerId " +
            "AND ia.availabilityDate BETWEEN :startDate AND :endDate " +
            "AND ia.isActive = true " +
            "ORDER BY ia.availabilityDate, ia.startTime")
    List<InterviewerAvailability> findAvailableSlots(
            Long interviewerId,
            LocalDate startDate,
            LocalDate endDate
    );

    // Get all interviewers available on a specific date and time range
    @Query("SELECT ia FROM InterviewerAvailability ia " +
            "WHERE ia.availabilityDate = :date " +
            "AND ia.startTime <= :requestedTime " +
            "AND ia.endTime >= :requestedEndTime " +
            "AND ia.isActive = true")
    List<InterviewerAvailability> findAvailableInterviewers(
            LocalDate date,
            LocalTime requestedTime,
            LocalTime requestedEndTime
    );
}