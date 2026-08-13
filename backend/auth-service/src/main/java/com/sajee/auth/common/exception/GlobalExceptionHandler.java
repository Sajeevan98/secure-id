package com.sajee.auth.common.exception;

import com.sajee.auth.common.api.ApiError;
import com.sajee.auth.common.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception, HttpServletRequest request) {

        ApiError error =
                new ApiError(exception.getCode(), exception.getMessage(), request.getRequestURI(), null);

        log.debug("Illegal operation: {}", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(error));
    }

    // Handles @Valid validation failures.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {

        List<String> details = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();

        log.debug("Request validation failed: {}", details);

        ApiError error = new ApiError(
                "VALIDATION_ERROR",
                "Request validation failed.",
                request.getRequestURI(),
                details
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(error));
    }

    // Handles rejection by database
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception, HttpServletRequest request) {

        log.debug("Conflicts with existing data: {}", exception.getMessage());

        ApiError error = new ApiError(
                "RESOURCE_CONFLICT",
                "The requested resource conflicts with existing data.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(error));
    }

    // Fallback handler.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleServer(Exception exception, HttpServletRequest request) {

        log.error("Unexpected error while processing request {} ", request.getRequestURI(), exception);

        ApiError error = new ApiError(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(error));
    }

}
