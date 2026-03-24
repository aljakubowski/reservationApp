package com.alja.reservation.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RoomUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleRoomUnavailable(RoomUnavailableException ex) {
        log.warn("Business validation failed: {}", ex.getMessage());
        var response = new ApiErrorResponse(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(BookingNotFoundException ex) {
        var response = new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLocking(OptimisticLockingFailureException ex) {
        log.warn("Concurrent booking attempt blocked by Optimistic Locking: {}", ex.getMessage());
        var response = new ApiErrorResponse(HttpStatus.CONFLICT.value(), "System Busy", "The system is processing a high volume of requests for this resource. Please try again.", null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        var response = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation Error", "Invalid request body", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        var response = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(), null);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(org.springframework.validation.BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(org.springframework.validation.BindException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        var response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Invalid request parameters",
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllUncaughtException(Exception ex) {
        log.error("Unknown internal server error occurred", ex);
        var response = new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "An unexpected error occurred.", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
