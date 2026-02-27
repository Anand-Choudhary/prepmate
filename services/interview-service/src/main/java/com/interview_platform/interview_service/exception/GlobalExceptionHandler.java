package com.interview_platform.interview_service.exception;

import com.interview_platform.interview_service.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SlotNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSlotNotFound(SlotNotFoundException ex, WebRequest request) {
        log.error("Slot not found: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse("SLOT_NOT_FOUND", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(SlotAlreadyBookedException.class)
    public ResponseEntity<ErrorResponse> handleSlotAlreadyBooked(SlotAlreadyBookedException ex, WebRequest request) {
        log.warn("Slot already booked: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse("SLOT_ALREADY_BOOKED", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(SlotNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleSlotNotAvailable(SlotNotAvailableException ex, WebRequest request) {
        log.warn("Slot not available: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse("SLOT_NOT_AVAILABLE", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DuplicateBookingException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateBooking(DuplicateBookingException ex, WebRequest request) {
        log.warn("Duplicate booking attempt: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse("DUPLICATE_BOOKING", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidBookingTimeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBookingTime(InvalidBookingTimeException ex, WebRequest request) {
        log.warn("Invalid booking time: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse("INVALID_BOOKING_TIME", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBooking(InvalidBookingException ex, WebRequest request) {
        log.warn("Invalid booking: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse("INVALID_BOOKING", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BookingLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleBookingLimitExceeded(BookingLimitExceededException ex, WebRequest request) {
        log.warn("Booking limit exceeded: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse("BOOKING_LIMIT_EXCEEDED", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookingNotFound(BookingNotFoundException ex, WebRequest request) {
        log.error("Booking not found: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse("BOOKING_NOT_FOUND", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, WebRequest request) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse("UNAUTHORIZED", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(InvalidCancellationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCancellation(InvalidCancellationException ex, WebRequest request) {
        log.warn("Invalid cancellation: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse("INVALID_CANCELLATION", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleResponseFromDownstream(SlotNotFoundException ex, WebRequest request) {
        log.error("Error from downstream: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse("Error from downstream", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidSlotException.class)
    public ResponseEntity<ErrorResponse> handleInvalidSlot(InvalidSlotException ex, WebRequest request) {
        log.warn("Invalid slot: {}", ex.getMessage());
        ErrorResponse error = buildErrorResponse("INVALID_SLOT", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

//    @ExceptionHandler(DistributedLockService.LockAcquisitionException.class)
//    public ResponseEntity<ErrorResponse> handleLockAcquisitionFailure(
//            DistributedLockService.LockAcquisitionException ex, WebRequest request) {
//        log.error("Lock acquisition failed: {}", ex.getMessage());
//        ErrorResponse error = buildErrorResponse("LOCK_ACQUISITION_FAILED",
//                "Too many concurrent requests. Please try again.", request);
//        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(error);
//    }
//
//    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
//    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(
//            ObjectOptimisticLockingFailureException ex, WebRequest request) {
//        log.warn("Optimistic locking failure: {}", ex.getMessage());
//        ErrorResponse error = buildErrorResponse("CONCURRENT_MODIFICATION",
//                "This slot was just modified. Please refresh and try again.", request);
//        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> handleValidationErrors(
//            MethodArgumentNotValidException ex, WebRequest request) {
//        String errors = ex.getBindingResult()
//                .getFieldErrors()
//                .stream()
//                .map(error -> error.getField() + ": " + error.getDefaultMessage())
//                .collect(Collectors.joining(", "));
//
//        log.warn("Validation error: {}", errors);
//        ErrorResponse error = buildErrorResponse("VALIDATION_ERROR", errors, request);
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
//    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred", ex);
        ErrorResponse error = buildErrorResponse("INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.", request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    private ErrorResponse buildErrorResponse(String error, String message, WebRequest request) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }
}