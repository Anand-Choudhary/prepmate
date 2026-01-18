package com.interview_platform.interview_service.controller;

import com.interview_platform.interview_service.dto.BookSlotRequest;
import com.interview_platform.interview_service.dto.BookingResponse;
import com.interview_platform.interview_service.dto.BookingSuccessResponse;
import com.interview_platform.interview_service.dto.RescheduleRequest;
import com.interview_platform.interview_service.service.InterviewBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@Slf4j
public class InterviewBookingController {

    private final InterviewBookingService bookingService;

    /**
     * Book an interview slot (Interviewee)
     * This endpoint has race condition handling via distributed locks + optimistic locking
     */
    @PostMapping("/slots/{slotId}/book")
    public ResponseEntity<BookingSuccessResponse> bookSlot(
            @PathVariable Long slotId,
            @RequestHeader("X-User-Id") Long intervieweeId,
            @Valid @RequestBody(required = false) BookSlotRequest request
    ) {
        log.info("Booking slot: {} by interviewee: {}", slotId, intervieweeId);
        BookingSuccessResponse response = bookingService.bookSlot(slotId, intervieweeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get booking details
     */
    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long bookingId) {
        log.info("Getting booking: {}", bookingId);
        BookingResponse response = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel a booking
     */
    @PostMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long bookingId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        log.info("Cancelling booking: {} by user: {}", bookingId, userId);
        bookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user's interviews as interviewee
     */
    @GetMapping("/my-interviews")
    public ResponseEntity<List<BookingResponse>> getMyInterviews(
            @RequestHeader("X-User-Id") Long userId
    ) {
        log.info("Getting interviews for interviewee: {}", userId);
        List<BookingResponse> bookings = bookingService.getUserBookings(userId, false);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get user's interviews as interviewer
     */
    @GetMapping("/my-conducted-interviews")
    public ResponseEntity<List<BookingResponse>> getMyConductedInterviews(
            @RequestHeader("X-User-Id") Long userId
    ) {
        log.info("Getting conducted interviews for interviewer: {}", userId);
        List<BookingResponse> bookings = bookingService.getUserBookings(userId, true);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Reschedule a booking (cancel old, book new)
     */
    @PostMapping("/bookings/{bookingId}/reschedule")
    public ResponseEntity<BookingSuccessResponse> rescheduleBooking(
            @PathVariable Long bookingId,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody RescheduleRequest request
    ) {
        log.info("Rescheduling booking: {} to slot: {} by user: {}",
                bookingId, request.getNewSlotId(), userId);

        // Cancel existing booking
        bookingService.cancelBooking(bookingId, userId);

        // Book new slot
        BookSlotRequest bookRequest = BookSlotRequest.builder()
                .notes(request.getNotes())
                .build();

        BookingSuccessResponse response = bookingService.bookSlot(
                request.getNewSlotId(), userId, bookRequest
        );

        return ResponseEntity.ok(response);
    }
}
