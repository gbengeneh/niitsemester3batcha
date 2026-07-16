package com.semester3.attendance.dto;

import com.semester3.attendance.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Used when an admin manually creates or corrects an attendance record,
 * e.g. marking someone ON_LEAVE or ABSENT, or backfilling a missed punch.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequest {

    @NotNull(message = "employeeId is required")
    private Long employeeId;

    @NotNull(message = "date is required")
    private LocalDate date;

    private LocalDateTime clockIn;

    private LocalDateTime clockOut;

    @NotNull(message = "status is required")
    private AttendanceStatus status;
}
