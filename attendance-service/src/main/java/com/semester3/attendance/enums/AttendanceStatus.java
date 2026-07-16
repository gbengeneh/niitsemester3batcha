package com.semester3.attendance.enums;

/**
 * Represents the daily attendance status of an employee.
 * The service computes/allows this to be set based on clock-in / clock-out times,
 * but it can also be set manually (e.g. ON_LEAVE, ABSENT) by an admin or by the
 * Leave Service via an event/callback in a more advanced setup.
 */
public enum AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    HALF_DAY,
    ON_LEAVE
}
