package com.semester3.attendance.exception;

/**
 * Thrown for invalid state transitions, e.g. clocking out before clocking in.
 */
public class InvalidAttendanceOperationException extends RuntimeException {
    public InvalidAttendanceOperationException(String message) {
        super(message);
    }
}
