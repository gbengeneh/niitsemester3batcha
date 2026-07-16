package com.semester3.attendance.dto;

import com.semester3.attendance.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private Long id;
    private Long employeeId;
    private LocalDate date;
    private LocalDateTime clockIn;
    private LocalDateTime clockOut;
    private AttendanceStatus status;
    private Long workedMinutes; // derived: minutes between clockIn and clockOut, if both present
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
