package com.interview_platform.interview_service.service;

import com.interview_platform.interview_service.dto.BookSlotRequest;
import com.interview_platform.interview_service.dto.BookingResponse;
import com.interview_platform.interview_service.dto.BookingSuccessResponse;
import com.interview_platform.interview_service.entity.InterviewBooking;
import com.interview_platform.interview_service.entity.InterviewSlot;
import com.interview_platform.interview_service.exception.*;
import com.interview_platform.interview_service.external.client.UserServiceClient;
import com.interview_platform.interview_service.external.client.VideoServiceClient;
import com.interview_platform.interview_service.external.dto.CreateVideoRoomRequest;
import com.interview_platform.interview_service.external.dto.VideoRoomResponse;
import com.interview_platform.interview_service.repository.InterviewBookingRepository;
import com.interview_platform.interview_service.repository.InterviewSlotRepository;
import com.interview_platform.interview_service.utils.BookingStatus;
import com.interview_platform.interview_service.utils.SlotStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterviewBookingService {

    private final InterviewSlotRepository slotRepository;
    private final InterviewBookingRepository bookingRepository;
    private final DistributedLockService lockService;
    private final RedisCacheService cacheService;
    private final VideoServiceClient videoServiceClient;
    private final UserServiceClient userServiceClient;
//    private final NotificationServiceClient notificationServiceClient;
//    private final InterviewEventPublisher eventPublisher;

    @Value("${interview.booking.min-advance-hours:1}")
    private int minAdvanceHours;

    @Value("${interview.booking.max-bookings-per-day:10}")
    private int maxBookingsPerDay;

    @Value("${interview.booking.min-cancellation-hours:2}")
    private int minCancellationHours;

    /**
     * Book an interview slot with race condition handling
     * Uses distributed locking + optimistic locking
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingSuccessResponse bookSlot(Long slotId, Long intervieweeId, BookSlotRequest request) {
        log.info("Booking request for slot: {} by interviewee: {}", slotId, intervieweeId);

        // Check for duplicate booking attempt (idempotency)
        if (cacheService.hasRecentBookingAttempt(intervieweeId, slotId)) {
            log.warn("Duplicate booking attempt detected for user: {}, slot: {}", intervieweeId, slotId);
            throw new DuplicateBookingException("You have already attempted to book this slot recently");
        }

        // Execute booking with distributed lock
        return lockService.executeWithLock(slotId, () -> {
            try {
                // Record booking attempt
                cacheService.recordBookingAttempt(intervieweeId, slotId);

                // Perform the booking
                BookingSuccessResponse response = performBooking(slotId, intervieweeId, request);

                // Clear booking attempt on success
                cacheService.clearBookingAttempt(intervieweeId, slotId);

                return response;

            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("Optimistic lock failure for slot: {} - likely already booked", slotId);
                throw new SlotAlreadyBookedException("This slot was just booked by another user");
            } catch (Exception e) {
                log.error("Error booking slot: {}", slotId, e);
                throw e;
            }
        });
    }

    /**
     * Perform actual booking (called within distributed lock)
     */
    private BookingSuccessResponse performBooking(Long slotId, Long intervieweeId, BookSlotRequest request) {
        // 1. Fetch and validate slot
        InterviewSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Interview slot not found: " + slotId));

        validateSlotForBooking(slot, intervieweeId);

        // 2. Check daily booking limit
//        long bookingsToday = slotRepository.countBookingsForIntervieweeOnDate(
//                intervieweeId, LocalDateTime.now()
//        );
//
//        if (bookingsToday >= maxBookingsPerDay) {
//            throw new BookingLimitExceededException(
//                    "You have reached the maximum bookings limit for today: " + maxBookingsPerDay
//            );
//        }

        // 3. Create video room
//        VideoServiceClient.VideoRoomResponse videoRoom = createVideoRoom(slot);

        // 4. Update slot status (with optimistic locking)
        slot.markAsBooked(intervieweeId);
        slot = slotRepository.save(slot); // Version will be incremented automatically

        // 5. Create booking record
        InterviewBooking booking = createBookingRecord(slot, request);
        booking = bookingRepository.save(booking);

        // 6. Invalidate cache
        cacheService.invalidateSlotsCache(slot.getInterviewerId());

        // 7. Publish event (async)
//        eventPublisher.publishInterviewBooked(
//                slot.getId(),
//                booking.getId(),
//                slot.getInterviewerId(),
//                intervieweeId,
//                booking.getBookingReference(),
//                slot.getStartTime(),
//                slot.getEndTime(),
//                slot.getMeetingLink(),
//                slot.getVideoRoomId()
//        );

        // 8. Send notifications (async with circuit breaker)
//        sendBookingNotifications(slot, booking);

        log.info("Successfully booked slot: {} for interviewee: {}, booking reference: {}",
                slotId, intervieweeId, booking.getBookingReference());

        return buildBookingSuccessResponse(slot, booking);
    }

    /**
     * Validate slot availability and booking conditions
     */
    private void validateSlotForBooking(InterviewSlot slot, Long intervieweeId) {
        // Check if slot is available
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotNotAvailableException("This slot is no longer available");
        }

        // Check if slot is in the future
        LocalDateTime minStartTime = LocalDateTime.now().plusHours(minAdvanceHours);
        if (slot.getStartTime().isBefore(minStartTime)) {
            throw new InvalidBookingTimeException(
                    "Slot must be booked at least " + minAdvanceHours + " hour(s) in advance"
            );
        }

        // Check if user is trying to book their own slot
        if (slot.getInterviewerId().equals(intervieweeId)) {
            throw new InvalidBookingException("You cannot book your own interview slot");
        }

        // Check for existing booking
        if (slotRepository.existsByIntervieweeIdAndSlotId(intervieweeId, slot.getId())) {
            throw new DuplicateBookingException("You have already booked this slot");
        }
    }

    /**
     * Create video room for the interview
     */
    @CircuitBreaker(name = "videoService", fallbackMethod = "createVideoRoomFallback")
    @Retry(name = "videoService")
    private VideoRoomResponse createVideoRoom(InterviewSlot slot) {
        CreateVideoRoomRequest request = CreateVideoRoomRequest.builder()
                .hostId(slot.getInterviewerId())
                .scheduledTime(slot.getStartTime())
                .durationMinutes(slot.getDurationMinutes())
                .interviewSlotId(String.valueOf(slot.getId()))
                .build();

        return videoServiceClient.createVideoRoom(request);
    }

    /**
     * Fallback for video room creation
     */
    private VideoRoomResponse createVideoRoomFallback(InterviewSlot slot, Exception e) {
        log.warn("Video service unavailable, using fallback for slot: {}", slot.getId());

        // Return mock video room data
        return VideoRoomResponse.builder()
                .roomId("fallback-" + slot.getId())
                .meetingLink("https://meet.example.com/fallback/" + slot.getId())
                .scheduledTime(slot.getStartTime())
                .status("PENDING")
                .build();
    }

    /**
     * Create booking record
     */
    private InterviewBooking createBookingRecord(InterviewSlot slot, BookSlotRequest request) {
        return InterviewBooking.builder()
                .slotId(slot.getId())
                .interviewerId(slot.getInterviewerId())
                .intervieweeId(slot.getIntervieweeId())
                .bookingStatus(BookingStatus.CONFIRMED)
                .notes(request != null ? request.getNotes() : null)
                .build();
    }

    /**
     * Send booking notifications
     */
//    @CircuitBreaker(name = "notificationService")
//    private void sendBookingNotifications(InterviewSlot slot, InterviewBooking booking) {
//        try {
//            // Notify interviewee
//            Map<String, Object> intervieweeData = new HashMap<>();
//            intervieweeData.put("bookingReference", booking.getBookingReference());
//            intervieweeData.put("interviewTitle", slot.getTitle());
//            intervieweeData.put("startTime", slot.getStartTime());
//            intervieweeData.put("meetingLink", slot.getMeetingLink());

//            notificationServiceClient.sendNotification(
//                    NotificationServiceClient.NotificationRequest.builder()
//                            .userId(slot.getIntervieweeId())
//                            .type("EMAIL")
//                            .template("interview_booked")
//                            .data(intervieweeData)
//                            .priority("HIGH")
//                            .build()
//            );

            // Notify interviewer
//            Map<String, Object> interviewerData = new HashMap<>();
//            interviewerData.put("bookingReference", booking.getBookingReference());
//            interviewerData.put("interviewTitle", slot.getTitle());
//            interviewerData.put("startTime", slot.getStartTime());

//            notificationServiceClient.sendNotification(
//                    NotificationServiceClient.NotificationRequest.builder()
//                            .userId(slot.getInterviewerId())
//                            .type("EMAIL")
//                            .template("interview_confirmed")
//                            .data(interviewerData)
//                            .priority("MEDIUM")
//                            .build()
//            );
//
//        } catch (Exception e) {
//            log.error("Failed to send booking notifications", e);
//            // Don't fail the booking if notifications fail
//        }
//    }

    /**
     * Build booking success response
     */
    private BookingSuccessResponse buildBookingSuccessResponse(InterviewSlot slot, InterviewBooking booking) {
        return BookingSuccessResponse.builder()
                .bookingId(String.valueOf(booking.getId()))
                .bookingReference(booking.getBookingReference())
                .slotId(slot.getId())
                .interviewerId(slot.getInterviewerId())
                .intervieweeId(slot.getIntervieweeId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
//                .meetingLink(slot.getMeetingLink())
//                .videoRoomId(slot.getVideoRoomId())
                .message("Interview booked successfully!")
                .bookedAt(slot.getBookedAt())
                .build();
    }

    /**
     * Cancel a booking
     */
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        InterviewBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        // Verify user has permission to cancel
        if (!booking.getIntervieweeId().equals(userId) && !booking.getInterviewerId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to cancel this booking");
        }

        InterviewSlot slot = slotRepository.findById(booking.getSlotId())
                .orElseThrow(() -> new SlotNotFoundException("Slot not found: " + booking.getSlotId()));

        // Check cancellation policy
        if (!slot.canBeCancelled(minCancellationHours)) {
            throw new InvalidCancellationException(
                    "Bookings must be cancelled at least " + minCancellationHours + " hours in advance"
            );
        }

        // Update slot status
        slot.markAsCancelled();
        slot.setIntervieweeId(null);
        slotRepository.save(slot);

        // Update booking status
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Invalidate cache
        cacheService.invalidateSlotsCache(slot.getInterviewerId());

        // Publish event
//        eventPublisher.publishInterviewCancelled(
//                slot.getId(),
//                booking.getId(),
//                slot.getInterviewerId(),
//                booking.getIntervieweeId()
//        );

        log.info("Cancelled booking: {} for slot: {}", bookingId, slot.getId());
    }

    /**
     * Get booking details
     */
    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId) {
        InterviewBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        InterviewSlot slot = slotRepository.findById(booking.getSlotId())
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        return buildBookingResponse(booking, slot);
    }

    /**
     * Get user's bookings
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId, boolean asInterviewer) {
        List<InterviewBooking> bookings;

        if (asInterviewer) {
            bookings = bookingRepository.findByInterviewerIdAndBookingStatusInOrderByCreatedAtDesc(
                    userId,
                    List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING)
            );
        } else {
            bookings = bookingRepository.findByIntervieweeIdAndBookingStatusInOrderByCreatedAtDesc(
                    userId,
                    List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING)
            );
        }

        return bookings.stream()
                .map(booking -> {
                    InterviewSlot slot = slotRepository.findById(booking.getSlotId()).orElse(null);
                    return buildBookingResponse(booking, slot);
                })
                .toList();
    }

    private BookingResponse buildBookingResponse(InterviewBooking booking, InterviewSlot slot) {
        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .slotId(booking.getSlotId())
                .interviewerId(booking.getInterviewerId())
                .intervieweeId(booking.getIntervieweeId())
//                .title(slot != null ? slot.getTitle() : null)
                .startTime(slot != null ? slot.getStartTime() : null)
                .endTime(slot != null ? slot.getEndTime() : null)
                .meetingLink(slot != null ? slot.getMeetingLink() : null)
                .videoRoomId(slot != null ? slot.getVideoRoomId() : null)
                .notes(booking.getNotes())
                .bookingStatus(booking.getBookingStatus().name())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}