package com.interview_platform.call_service.service;

import com.interview_platform.call_service.dto.InvoiceDto;
import com.interview_platform.call_service.dto.MinuteQualityDto;
import com.interview_platform.call_service.dto.QualityStatistics;
import com.interview_platform.call_service.dto.RealtimeQualityStatistics;
import com.interview_platform.call_service.entity.*;
import com.interview_platform.call_service.exception.ResourceNotFoundException;
import com.interview_platform.call_service.repository.InterviewRoomRepository;
import com.interview_platform.call_service.repository.MinuteQualityLogRepository;
import com.interview_platform.call_service.repository.RoomEventRepository;
import com.interview_platform.call_service.repository.RoomParticipantRepository;
import com.interview_platform.call_service.utils.EventType;
import com.interview_platform.call_service.utils.RoomStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * HYBRID APPROACH - Billing Service
 *
 * During call: Everything in Redis (fast, real-time)
 * End of call: Batch write all data to database (safe, persistent)
 *
 * Flow:
 * 1. Call starts → Initialize in Redis
 * 2. Every minute → Store quality data in Redis
 * 3. Call ends → Batch write ALL minutes to database + calculate billing
 * 4. Clear Redis cache
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final InterviewRoomRepository roomRepository;
//    private final MinuteQualityLogRepository qualityLogRepository;
    private final RoomParticipantRepository participantRepository;
    private final RoomEventRepository eventRepository;
    private final QualityCacheService qualityCacheService;


    @Transactional
    public void startBillingSession(String roomToken) {
        InterviewRoom room = roomRepository.findByRoomToken(roomToken)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomToken));

        // Update room status in database (this is metadata, not quality data)
        room.setStartedAt(LocalDateTime.now());
        room.setStatus(RoomStatus.ACTIVE);
        roomRepository.save(room);

        // Initialize Redis cache for this room
//        qualityCacheService.initializeRoomCache(room);

        // Store call start time in Redis
//        qualityCacheService.cacheRoomData(roomToken, "startTime", LocalDateTime.now());
//        qualityCacheService.cacheRoomData(roomToken, "currentMinute", 0);

        // Log event (metadata)
        logEvent(roomToken, null, EventType.ROOM_STARTED, "Billing session started");

        log.info("Started billing session for room {} - Scheduled: {} minutes @ ₹{}/min = ₹{}",
                roomToken, room.getScheduledDurationMinutes(), room.getRatePerMinute(), room.getScheduledAmount());
    }

    /**
     * Log quality metrics for a specific minute
     * REDIS ONLY - Store in cache, don't write to database yet
     */
    public void logMinuteQuality(MinuteQualityDto qualityDTO) {
        String roomId = qualityDTO.getRoomId().toString();

        // Store minute quality in Redis (temporary storage)
        qualityCacheService.cacheMinuteQuality(
                roomId,
                qualityDTO.getUserId(),
                qualityDTO.getMinuteNumber(),
                qualityDTO
        );

        // Update counters in Redis
        if (Boolean.TRUE.equals(qualityDTO.getIsGoodQuality())) {
            qualityCacheService.incrementQualityCounter(roomId, "good_minutes");
            qualityCacheService.incrementQualityCounter(roomId, "user:" + qualityDTO.getUserId() + ":good_minutes");
        } else {
            qualityCacheService.incrementQualityCounter(roomId, "poor_minutes");
            qualityCacheService.incrementQualityCounter(roomId, "user:" + qualityDTO.getUserId() + ":poor_minutes");

            // Add alert for poor quality
            qualityCacheService.addQualityAlert(
                    roomId,
                    qualityDTO.getIssueType(),
                    String.format("User %d - Minute %d: %s",
                            qualityDTO.getUserId(), qualityDTO.getMinuteNumber(), qualityDTO.getIssueDescription())
            );
        }

        log.debug("Cached minute {} quality for room {} (Redis only) - Quality: {} ({})",
                qualityDTO.getMinuteNumber(), roomId,
                qualityDTO.getIsGoodQuality() ? "GOOD" : "POOR", qualityDTO.getQualityRating());
    }

    /**
     * End billing session and BATCH WRITE all data to database
     * This is the CRITICAL method for Hybrid approach
     */
    @Transactional
    public InterviewRoom endBillingSession(String roomToken) {
        log.info("Ending billing session for room {} - Starting batch write to database", roomToken);

        InterviewRoom room = roomRepository.findByRoomToken(roomToken)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomToken));

        room.setEndedAt(LocalDateTime.now());
        room.setStatus(RoomStatus.COMPLETED);

        // Calculate actual duration
        Duration duration = Duration.between(room.getStartedAt(), room.getEndedAt());
        int actualMinutes = (int) duration.toMinutes();
        room.setActualDurationMinutes(actualMinutes);

        try {
            // STEP 1: Retrieve ALL minute quality data from Redis
            List<MinuteQualityDto> allMinuteQualities = retrieveAllMinuteQualitiesFromRedis(roomToken);

            log.info("Retrieved {} minute quality records from Redis for room {}",
                    allMinuteQualities.size(), roomToken);

            // STEP 2: Convert DTOs to entities
            List<MinuteQualityLog> qualityLogs = allMinuteQualities.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());

            // STEP 3: BATCH WRITE all minute qualities to database in ONE transaction
            if (!qualityLogs.isEmpty()) {
                qualityLogRepository.saveAll(qualityLogs);
                log.info("Batch saved {} minute quality logs to database", qualityLogs.size());
            }

            // STEP 4: Calculate billing metrics from the data
            long goodMinutes = allMinuteQualities.stream()
                    .filter(q -> Boolean.TRUE.equals(q.getIsGoodQuality()))
                    .count();
            long poorMinutes = allMinuteQualities.stream()
                    .filter(q -> Boolean.FALSE.equals(q.getIsGoodQuality()))
                    .count();

            room.setGoodQualityMinutes((int) goodMinutes);
            room.setPoorQualityMinutes((int) poorMinutes);

            // Calculate billing - charge only for good quality minutes
            double billableMinutes = (double) goodMinutes;
            double finalAmount = billableMinutes * room.getRatePerMinute();
            double discount = room.getScheduledAmount() - finalAmount;

            room.setBillableMinutes(billableMinutes);
            room.setFinalAmount(finalAmount);
            room.setQualityDiscount(discount);

            // STEP 5: Update participant-specific billing data
            updateParticipantBillingData(roomToken, allMinuteQualities, room.getRatePerMinute());

            // STEP 6: Save room with final billing data
            room = roomRepository.save(room);

            // STEP 7: Log billing completion event
            logEvent(roomToken, null, EventType.BILLING_CALCULATED,
                    String.format("Final: ₹%.2f, Discount: ₹%.2f (Good: %d min, Poor: %d min)",
                            finalAmount, discount, goodMinutes, poorMinutes));

            log.info("Billing session ended for room {} - Total: {} min, Good: {} min, Poor: {} min",
                    roomToken, actualMinutes, goodMinutes, poorMinutes);
            log.info("Billing: Scheduled ₹{} → Final ₹{} (Discount: ₹{})",
                    room.getScheduledAmount(), finalAmount, discount);

        } catch (Exception e) {
            log.error("Error processing billing for room {}", roomToken, e);

            // Even if billing fails, mark room as completed
            room = roomRepository.save(room);

            throw new RuntimeException("Failed to process billing: " + e.getMessage(), e);
        } finally {
            // STEP 8: ALWAYS clean up Redis cache after successful database write
            qualityCacheService.cleanupRoomCache(roomToken);
            log.info("Cleaned up Redis cache for room {}", roomToken);
        }

        return room;
    }


    private List<MinuteQualityDto> retrieveAllMinuteQualitiesFromRedis(String roomToken) {
        List<MinuteQualityDto> allQualities = new ArrayList<>();


        // Get all participants
        List<RoomParticipant> participants = participantRepository.findByRoomToken(roomToken);

        for (RoomParticipant participant : participants) {
            Long userId = participant.getUserId();

            // Get total minutes from Redis counter or actual minute count
            Long totalMinutesObj = qualityCacheService.getQualityCounter(roomToken,
                    "user:" + userId + ":total_minutes");
            int totalMinutes = totalMinutesObj != null ? totalMinutesObj.intValue() : 30; // Default max

            // Retrieve each minute's quality data
            for (int minute = 1; minute <= totalMinutes; minute++) {
                MinuteQualityDto quality = qualityCacheService.getCachedMinuteQuality(
                        roomToken, userId, minute);

                if (quality != null) {
                    allQualities.add(quality);
                }
            }
        }

        return allQualities;
    }


    private MinuteQualityLog convertToEntity(MinuteQualityDto dto) {
        MinuteQualityLog entity = new MinuteQualityLog();
        entity.setRoomId(dto.getRoomId());
        entity.setUserId(dto.getUserId());
        entity.setMinuteNumber(dto.getMinuteNumber());
        entity.setMinuteStartTime(dto.getMinuteStartTime());
        entity.setMinuteEndTime(dto.getMinuteEndTime());
        entity.setIsGoodQuality(dto.getIsGoodQuality());
        entity.setQualityRating(dto.getQualityRating());
        entity.setAveragePacketLoss(dto.getAveragePacketLoss());
        entity.setAverageJitter(dto.getAverageJitter());
        entity.setAverageRTT(dto.getAverageRTT());
        entity.setAverageFrameRate(dto.getAverageFrameRate());
        entity.setAverageBitrate(dto.getAverageBitrate());
        entity.setVideoWidth(dto.getVideoWidth());
        entity.setVideoHeight(dto.getVideoHeight());
        entity.setVideoCodec(dto.getVideoCodec());
        entity.setAudioCodec(dto.getAudioCodec());
        entity.setIssueType(dto.getIssueType());
        entity.setIssueDescription(dto.getIssueDescription());
        entity.setSecondsConnected(dto.getSecondsConnected());
        entity.setSecondsDisconnected(dto.getSecondsDisconnected());
        return entity;
    }


    @Transactional
    private void updateParticipantBillingData(String roomToken, List<MinuteQualityDto> allQualities, Double ratePerMinute) {
        // Group qualities by user
        Map<Long, List<MinuteQualityDto>> qualitiesByUser = allQualities.stream()
                .collect(Collectors.groupingBy(MinuteQualityDto::getUserId));

        // Update each participant
        for (Map.Entry<Long, List<MinuteQualityDto>> entry : qualitiesByUser.entrySet()) {
            Long userId = entry.getKey();
            List<MinuteQualityDto> userQualities = entry.getValue();

            RoomParticipant participant = participantRepository.findByRoomIdAndUserId(roomToken, userId)
                    .orElseThrow(() -> new RuntimeException("Participant not found: " + userId));

            long goodMinutes = userQualities.stream()
                    .filter(q -> Boolean.TRUE.equals(q.getIsGoodQuality()))
                    .count();
            long poorMinutes = userQualities.stream()
                    .filter(q -> Boolean.FALSE.equals(q.getIsGoodQuality()))
                    .count();

            participant.setParticipantGoodMinutes((int) goodMinutes);
            participant.setParticipantPoorMinutes((int) poorMinutes);
            participant.setParticipantDiscount(poorMinutes * ratePerMinute);

            // Update connection quality status
            participant.updateConnectionQualityStatus();

            participantRepository.save(participant);
        }
    }

    /**
     * Generate detailed invoice (reads from DATABASE, not Redis)
     */
    @Transactional(readOnly = true)
    public InvoiceDto generateInvoice(String roomToken) {
        InterviewRoom room = roomRepository.findByRoomToken(roomToken)
                .orElseThrow(() -> new RuntimeException("Room not found: " + roomToken));

        // Read from DATABASE (not Redis)
        List<MinuteQualityLog> qualityLogs = qualityLogRepository.findByRoomTokenOrderByMinuteNumber(roomToken);
        List<RoomParticipant> participants = participantRepository.findByRoomToken(roomToken);

        Double avgPacketLoss = qualityLogRepository.getAveragePacketLoss(roomToken);
        Double avgJitter = qualityLogRepository.getAverageJitter(roomToken);
        Double avgRTT = qualityLogRepository.getAverageRTT(roomToken);

        InvoiceDto invoice = new InvoiceDto();
        invoice.setRoom(room);
        invoice.setQualityLogs(qualityLogs);
        invoice.setParticipants(participants);
        invoice.setAveragePacketLoss(avgPacketLoss != null ? avgPacketLoss : 0.0);
        invoice.setAverageJitter(avgJitter != null ? avgJitter : 0.0);
        invoice.setAverageRTT(avgRTT != null ? avgRTT : 0.0);

        return invoice;
    }

    /**
     * Get minute-by-minute breakdown for a room (from DATABASE)
     */
    @Transactional(readOnly = true)
    public List<MinuteQualityLog> getMinuteBreakdown(String roomToken) {
        return qualityLogRepository.findByRoomTokenOrderByMinuteNumber(roomToken);
    }

    /**
     * Get minute breakdown for specific user (from DATABASE)
     */
    @Transactional(readOnly = true)
    public List<MinuteQualityLog> getUserMinuteBreakdown(String roomToken, Long userId) {
        return qualityLogRepository.findByRoomTokenAndUserIdOrderByMinuteNumber(roomToken, userId);
    }

    /**
     * Get quality statistics for a room (from DATABASE)
     */
    @Transactional(readOnly = true)
    public QualityStatistics getQualityStatistics(String roomToken) {
        Long goodMinutes = qualityLogRepository.countGoodQualityMinutes(roomToken);
        Long poorMinutes = qualityLogRepository.countPoorQualityMinutes(roomToken);
        Double avgPacketLoss = qualityLogRepository.getAveragePacketLoss(roomToken);
        Double avgJitter = qualityLogRepository.getAverageJitter(roomToken);
        Double avgRTT = qualityLogRepository.getAverageRTT(roomToken);
        List<MinuteQualityLog> worstMinutes = qualityLogRepository.findWorstMinutes(roomToken);

        QualityStatistics stats = new QualityStatistics();
        stats.setGoodMinutes(goodMinutes != null ? goodMinutes.intValue() : 0);
        stats.setPoorMinutes(poorMinutes != null ? poorMinutes.intValue() : 0);
        stats.setAveragePacketLoss(avgPacketLoss != null ? avgPacketLoss : 0.0);
        stats.setAverageJitter(avgJitter != null ? avgJitter : 0.0);
        stats.setAverageRTT(avgRTT != null ? avgRTT : 0.0);
        stats.setWorstMinute(worstMinutes.isEmpty() ? null : worstMinutes.get(0));

        return stats;
    }

    /**
     * Get real-time quality statistics from Redis (during active call)
     */
    public RealtimeQualityStatistics getRealtimeQualityStatistics(UUID roomId) {
        String roomIdStr = roomId.toString();

        Long goodMinutes = qualityCacheService.getQualityCounter(roomIdStr, "good_minutes");
        Long poorMinutes = qualityCacheService.getQualityCounter(roomIdStr, "poor_minutes");
        List<Object> alerts = qualityCacheService.getQualityAlerts(roomIdStr);

        RealtimeQualityStatistics stats = new RealtimeQualityStatistics();
        stats.setGoodMinutes(goodMinutes != null ? goodMinutes.intValue() : 0);
        stats.setPoorMinutes(poorMinutes != null ? poorMinutes.intValue() : 0);
        stats.setRecentAlerts(alerts);

        return stats;
    }

    /**
     * Log room event
     */
    private void logEvent(String roomToken, Long userId, EventType eventType, String metadata) {
        RoomEvent event = new RoomEvent();
        event.setRoomToken(roomToken);
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setMetadata(metadata);
        event.setTimestamp(LocalDateTime.now());
        eventRepository.save(event);
    }


    public void processCallEnded(CallEndedEvent event, Acknowledgment ack) {
        String roomId = event.getRoomId();

        try {
            log.info("=== Processing billing for room: {} ===", roomId);

            // Step 1: Try to fetch from Redis (fast path)
            List<MinuteQualityDto> qualityData = fetchFromRedis(roomId);

            // Step 2: Fetch from fallback DB (if any data was stored there)
            List<MinuteQualityDto> fallbackData = fetchFromFallbackDB(
                    roomId,
                    event.getStartTime(),
                    event.getEndTime()
            );

            // Merge fallback data with Redis data
            if (!fallbackData.isEmpty()) {
                log.info("Found {} records in fallback DB for room: {}",
                        fallbackData.size(), roomId);
                qualityData.addAll(fallbackData);

                // Sort by timestamp
                qualityData.sort((a, b) -> a.getMinuteNumber().compareTo(b.getMinuteNumber()));
            }

            // Step 3: Validate data completeness
            int expectedMinutes = event.getDuration();
            int actualMinutes = qualityData.size();

            log.info("Expected {} minutes, found {} minutes total (Redis + Fallback DB)",
                    expectedMinutes, actualMinutes);

            // Step 4: If still incomplete, replay from Kafka
            if (actualMinutes < expectedMinutes * 0.9) {
                log.warn("Incomplete data for room: {}. Expected: {}, Got: {}. " +
                                "Falling back to Kafka replay",
                        roomId, expectedMinutes, actualMinutes);

                qualityData = replayFromKafka(roomId, event.getStartTime(), event.getEndTime());

                log.info("After Kafka replay: {} minutes of quality data", qualityData.size());
            }

            // Step 5: Calculate billing
            Bill bill = calculateBilling(event, qualityData);

            // Step 6: Save to database
            billRepository.save(bill);
            log.info("✅ Bill saved for room: {} - Amount: ${}", roomId, bill.getTotalCost());

            // Step 7: Cleanup
            cleanupRedis(roomId);
            cleanupFallbackDB(roomId);

            // Step 8: Acknowledge
            ack.acknowledge();

            log.info("=== Billing completed successfully for room: {} ===", roomId);

        } catch (Exception e) {
            log.error("❌ Failed to process billing for room: {}", roomId, e);
            throw e;
        }
    }

    /**
     * Fetch quality data from fallback DB
     */
    private List<MinuteQualityDto> fetchFromFallbackDB(
            String roomId,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        try {
            List<QualityDataFallback> fallbacks =
                    fallbackRepository.findByRoomIdAndTimestampBetween(
                            roomId,
                            startTime,
                            endTime
                    );

            return fallbacks.stream()
                    .map(this::convertFallbackToDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to fetch from fallback DB for room: {}", roomId, e);
            return new ArrayList<>();
        }
    }





}