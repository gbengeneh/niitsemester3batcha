package com.semester3.payroll_services.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Consistent error shape returned by GlobalExceptionHandler for all failures.
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    Map<String, String> fieldErrors // null unless it's a validation failure
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, null);
    }

    public static ErrorResponse ofValidation(int status, String error, Map<String, String> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status, error, "Validation failed", fieldErrors);
    }
}
