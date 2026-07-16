package com.semester3.attendance.service;

import com.semester3.attendance.dto.AttendanceRequest;
import com.semester3.attendance.dto.AttendanceResponse;
import com.semester3.attendance.dto.ClockInRequest;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    AttendanceResponse clockIn(ClockInRequest request);

    AttendanceResponse clockOut(Long employeeId);

    AttendanceResponse createOrUpdateManually(AttendanceRequest request);

    AttendanceResponse getById(Long id);

    List<AttendanceResponse> getAll();

    List<AttendanceResponse> getByEmployeeId(Long employeeId);

    AttendanceResponse getByEmployeeIdAndDate(Long employeeId, LocalDate date);

    List<AttendanceResponse> getByEmployeeIdAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate);

    List<AttendanceResponse> getByDate(LocalDate date);

    AttendanceResponse updateStatus(Long id, com.semester3.attendance.enums.AttendanceStatus status);

    void delete(Long id);
}
