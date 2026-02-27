package com.interview_platform.call_service.exception;

import com.interview_platform.call_service.dto.ApiResponse;
import com.interview_platform.call_service.dto.ErrorDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler
{


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {

        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Input validation failed")
                .path(getPath(request))
                .fieldErrors(fieldErrors)
                .build();

        ApiResponse<ErrorDetails> response = ApiResponse.error(
                "Validation failed for one or more fields",
                "VALIDATION_ERROR"
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }




    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        log.error("Resource not found: {}", ex.getMessage());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        ApiResponse<ErrorDetails> response = ApiResponse.error(
                ex.getMessage(),
                "RESOURCE_NOT_FOUND"
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleBadRequestException(
            BadRequestException ex, WebRequest request) {

        log.error("Bad request: {}", ex.getMessage());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        if (ex.getField() != null) {
            Map<String, String> fieldErrors = new HashMap<>();
            fieldErrors.put(ex.getField(), ex.getMessage());
            errorDetails.setFieldErrors(fieldErrors);
        }

        ApiResponse<ErrorDetails> response = ApiResponse.error(
                ex.getMessage(),
                "BAD_REQUEST"
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RoomAccessException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleRoomAccessException(
            RoomAccessException ex, WebRequest request) {

        log.error("Room access denied: {}",  ex.getMessage());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Forbidden")
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        ApiResponse<ErrorDetails> response = ApiResponse.error(
                ex.getMessage(),
                "ACCESS_DENIED"
        );

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(KafkaTimeoutException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleKafkaTimeoutException(
            KafkaTimeoutException ex, WebRequest request) {

        log.error("Kafka timeout occurred: {}", ex.getMessage(), ex);

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                .message("Kafka broker is not available. Please try again later.")
                .path(getPath(request))
                .build();

        ApiResponse<ErrorDetails> response = ApiResponse.error(
                "Kafka broker timeout",
                "KAFKA_TIMEOUT"
        );

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }



    @ExceptionHandler(InternalServerError.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleInternalServerError(
            InternalServerError ex, WebRequest request) {

        log.error("Internal server error: {}",  ex.getMessage(), ex);

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        ApiResponse<ErrorDetails> response = ApiResponse.error(
                "An internal error occurred. Please contact support",
                "INTERNAL_SERVER_ERROR"
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {

        log.error("Data integrity violation: {}",  ex.getMessage());

        String userMessage = "Data integrity constraint violated";
        if (ex.getMessage().contains("duplicate") || ex.getMessage().contains("unique")) {
            userMessage = "A record with this information already exists";
        } else if (ex.getMessage().contains("foreign key")) {
            userMessage = "Referenced entity does not exist";
        }

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Data Integrity Violation")
                .message(userMessage)
                .path(getPath(request))
                .build();

        ApiResponse<ErrorDetails> response = ApiResponse.error(
                userMessage,
                "DATA_INTEGRITY_VIOLATION"
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleDataAccessException(
            DataAccessException ex, WebRequest request) {

        log.error("Database error: {}", ex.getMessage(), ex);

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Database Error")
                .message("A database error occurred")
                .path(getPath(request))
                .build();

        ApiResponse<ErrorDetails> response = ApiResponse.error(
                "Database operation failed. Please contact support with trace ID: " ,
                "DATABASE_ERROR"
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle 404 - No Handler Found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleNoHandlerFound(
            NoHandlerFoundException ex, WebRequest request) {

        log.warn("No handler found for: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL()))
                .path(getPath(request))
                .build();

        ApiResponse<ErrorDetails> response = ApiResponse.error(
                "The requested endpoint does not exist",
                "ENDPOINT_NOT_FOUND"
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorDetails>> handleGlobalException(
            Exception ex, WebRequest request) {

        log.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorDetails errorDetails = ErrorDetails.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(getPath(request))
                .build();

        ApiResponse<ErrorDetails> response = ApiResponse.error(
                "An unexpected error occurred. Please contact support" ,
                "UNEXPECTED_ERROR"
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }


}
