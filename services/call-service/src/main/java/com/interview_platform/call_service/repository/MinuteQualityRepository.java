package com.interview_platform.call_service.repository;

import com.interview_platform.call_service.entity.MinuteQuality;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MinuteQualityRepository extends JpaRepository<MinuteQuality, Long> {

    List<MinuteQuality> findByRoomTokenAndUserIdOrderByMinuteNumber(
            String roomToken,
            Long userId
    );


    MinuteQuality findByRoomTokenAndUserIdAndMinuteNumber(
            String roomToken,
            Long userId,
            Integer minuteNumber
    );
}