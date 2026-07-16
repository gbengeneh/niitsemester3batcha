package com.semester3.attendance.exception;

/**
 * Thrown when trying to clock in twice for the same employee on the same day,
 * or create a record that already exists for that employee/date pair.
 */
public class DuplicateAttendanceException extends RuntimeException {
    public DuplicateAttendanceException(String message) {
        super(message);
    }
}
