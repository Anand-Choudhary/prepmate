package com.interview_platform.interview_service.controller;

import com.interview_platform.interview_service.dto.*;
import com.interview_platform.interview_service.security.UserContext;
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


    @PostMapping("/slots/{slotId}/book")
    public ResponseEntity<ApiResponse<BookingSuccessResponse>> bookSlot(
            @PathVariable Long slotId,
            @Valid @RequestBody(required = false) BookSlotRequest request
    ) {
        Long intervieweeId = UserContext.getUserId();
        log.info("Booking slot: {} by interviewee: {}", slotId, intervieweeId);
        BookingSuccessResponse response = bookingService.bookSlot(slotId, intervieweeId, request);
        return ResponseEntity.ok(ApiResponse.success("Slot booked successfully", response));
    }


    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long bookingId) {
        log.info("Getting booking: {}", bookingId);
        BookingResponse response = bookingService.getBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success("Booking details fetched", response));
    }


    @PostMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<Boolean>> cancelBooking(
            @PathVariable Long bookingId
    ) {
        Long userId = UserContext.getUserId();
        log.info("Cancelling booking: {} by user: {}", bookingId, userId);
        bookingService.cancelBooking(bookingId, userId);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled", Boolean.TRUE));
    }


    @GetMapping("/my-interviews")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyInterviews()
    {
        Long userId = UserContext.getUserId();
        log.info("Getting interviews for interviewee: {}", userId);
        List<BookingResponse> bookings = bookingService.getUserBookings(userId, false);
        return ResponseEntity.ok(ApiResponse.success("Booking details fetched", bookings));
    }

    @GetMapping("/my-conducted-interviews")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyConductedInterviews(
    ) {
        Long userId = UserContext.getUserId();
        log.info("Getting conducted interviews for interviewer: {}", userId);
        List<BookingResponse> bookings = bookingService.getUserBookings(userId, true);
        return ResponseEntity.ok(ApiResponse.success("Booking details fetched", bookings));
    }


    @PostMapping("/bookings/{bookingId}/reschedule")
    public ResponseEntity<ApiResponse<BookingSuccessResponse>> rescheduleBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody RescheduleRequest request
    ) {
        Long userId = UserContext.getUserId();
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

        return ResponseEntity.ok(ApiResponse.success("Booking rescheduled successfully", response));
    }
}
