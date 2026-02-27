package com.interview_platform.interview_service.service;

import com.interview_platform.interview_service.dto.ApiResponse;
import com.interview_platform.interview_service.dto.BookSlotRequest;
import com.interview_platform.interview_service.dto.BookingResponse;
import com.interview_platform.interview_service.dto.BookingSuccessResponse;
import com.interview_platform.interview_service.entity.InterviewBooking;
import com.interview_platform.interview_service.entity.InterviewSlot;
import com.interview_platform.interview_service.exception.*;
import com.interview_platform.interview_service.external.client.VideoServiceClient;
import com.interview_platform.interview_service.external.dto.CreateRoomRequest;
import com.interview_platform.interview_service.external.dto.RoomResponse;
import com.interview_platform.interview_service.external.handler.ApiResponseHandler;
import com.interview_platform.interview_service.repository.InterviewBookingRepository;
import com.interview_platform.interview_service.repository.InterviewSlotRepository;
import com.interview_platform.interview_service.utils.BookingStatus;
import com.interview_platform.interview_service.utils.SlotStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InterviewBookingService {

    private final InterviewSlotRepository slotRepository;
    private final InterviewBookingRepository bookingRepository;
    private final DistributedLockService lockService;
    private final RedisCacheService cacheService;
    private final VideoServiceClient videoServiceClient;
    private final ApiResponseHandler apiResponseHandler;


    @Value("${interview.booking.min-advance-hours:1}")
    private int minAdvanceHours;


    @Value("${interview.booking.min-cancellation-hours:2}")
    private int minCancellationHours;


    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingSuccessResponse bookSlot(Long slotId, Long intervieweeId, BookSlotRequest request) {
        log.info("Booking request for slot: {} by interviewee: {}", slotId, intervieweeId);

        if (cacheService.hasRecentBookingAttempt(intervieweeId, slotId)) {
            log.warn("Duplicate booking attempt detected for user: {}, slot: {}", intervieweeId, slotId);
            throw new DuplicateBookingException("You have already attempted to book this slot recently");
        }


        return lockService.executeWithLock(slotId, () -> {
            try {
                cacheService.recordBookingAttempt(intervieweeId, slotId);

                BookingSuccessResponse response = performBooking(slotId, intervieweeId, request);

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

    private BookingSuccessResponse performBooking(Long slotId, Long intervieweeId, BookSlotRequest request) {
        InterviewSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new SlotNotFoundException("Interview slot not found: " + slotId));

        String bookingReference = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        validateSlotForBooking(slot, intervieweeId);

        slot.markAsBooked(intervieweeId);
        slot = slotRepository.save(slot);


        InterviewBooking booking = createBookingRecord(slot, request);
        booking.setBookingReference(bookingReference);

        RoomResponse room = apiResponseHandler.unwrap(
               createVideoRoom(slot, booking)
        );


//        RoomResponse room = createVideoRoom(slot,booking);
        if(room.getRoomToken() != null)
        {
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            booking.setRoomToken(room.getRoomToken());
            booking=bookingRepository.save(booking);
        }

        cacheService.invalidateSlotsCache(slot.getInterviewerId());


        log.info("Successfully booked slot: {} for interviewee: {}, booking reference: {}",
                slotId, intervieweeId, booking.getBookingReference());

        return buildBookingSuccessResponse(slot, booking);
    }


    private void validateSlotForBooking(InterviewSlot slot, Long intervieweeId) {
        // Check if slot is available
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new SlotNotAvailableException("This slot is no longer available");
        }


        LocalDateTime minStartTime = LocalDateTime.now().plusHours(minAdvanceHours);
        if (slot.getStartTime().isBefore(minStartTime)) {
            throw new InvalidBookingTimeException(
                    "Slot must be booked at least " + minAdvanceHours + " hour(s) in advance"
            );
        }


        if (slot.getInterviewerId().equals(intervieweeId)) {
            throw new InvalidBookingException("You cannot book your own interview slot");
        }


        if (slotRepository.existsByIntervieweeIdAndSlotId(intervieweeId, slot.getId())) {
            throw new DuplicateBookingException("You have already booked this slot");
        }
    }

    private ApiResponse<RoomResponse> createVideoRoom(InterviewSlot slot, InterviewBooking booking) {
        CreateRoomRequest request = CreateRoomRequest.builder()
                .interviewerId(slot.getInterviewerId())
                .intervieweeId(slot.getIntervieweeId())
                .bookingReference(booking.getBookingReference())
                .scheduledAt(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .maxDurationMinutes(slot.getDurationMinutes())
                .build();

        return videoServiceClient.createVideoRoom(request);
    }


    private InterviewBooking createBookingRecord(InterviewSlot slot, BookSlotRequest request) {
        return InterviewBooking.builder()
                .slotId(slot.getId())
                .interviewerId(slot.getInterviewerId())
                .intervieweeId(slot.getIntervieweeId())
                .bookingStatus(BookingStatus.PENDING)
                .notes(request != null ? request.getNotes() : null)
                .build();
    }




    private BookingSuccessResponse buildBookingSuccessResponse(InterviewSlot slot, InterviewBooking booking) {
        return BookingSuccessResponse.builder()
                .bookingId(String.valueOf(booking.getId()))
                .bookingReference(booking.getBookingReference())
                .slotId(slot.getId())
                .interviewerId(slot.getInterviewerId())
                .intervieweeId(slot.getIntervieweeId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .message("Interview booked successfully!")
                .bookedAt(slot.getBookedAt())
                .build();
    }


    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        InterviewBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));


        if (!booking.getIntervieweeId().equals(userId) && !booking.getInterviewerId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to cancel this booking");
        }

        InterviewSlot slot = slotRepository.findById(booking.getSlotId())
                .orElseThrow(() -> new SlotNotFoundException("Slot not found: " + booking.getSlotId()));


        if (!slot.canBeCancelled(minCancellationHours)) {
            throw new InvalidCancellationException(
                    "Bookings must be cancelled at least " + minCancellationHours + " hours in advance"
            );
        }


        slot.markAsCancelled();
        slot.setIntervieweeId(null);
        slotRepository.save(slot);


        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);


        cacheService.invalidateSlotsCache(slot.getInterviewerId());


        log.info("Cancelled booking: {} for slot: {}", bookingId, slot.getId());
    }


    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId) {
        InterviewBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        InterviewSlot slot = slotRepository.findById(booking.getSlotId())
                .orElseThrow(() -> new SlotNotFoundException("Slot not found"));

        return buildBookingResponse(booking, slot);
    }


    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId, boolean asInterviewer) {
        List<InterviewBooking> bookings;

        if (asInterviewer) {
            bookings = bookingRepository.findByInterviewerIdAndBookingStatusInOrderByCreatedAtDesc(
                    userId,
                    List.of(BookingStatus.CONFIRMED)
            );
        } else {
            bookings = bookingRepository.findByIntervieweeIdAndBookingStatusInOrderByCreatedAtDesc(
                    userId,
                    List.of(BookingStatus.CONFIRMED)
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
                .startTime(slot != null ? slot.getStartTime() : null)
                .endTime(slot != null ? slot.getEndTime() : null)
                .notes(booking.getNotes())
                .bookingStatus(booking.getBookingStatus().name())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}