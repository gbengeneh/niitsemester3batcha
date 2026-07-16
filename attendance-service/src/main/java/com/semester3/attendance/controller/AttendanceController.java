package com.semester3.attendance.controller;

import com.semester3.attendance.dto.AttendanceRequest;
import com.semester3.attendance.dto.AttendanceResponse;
import com.semester3.attendance.dto.ClockInRequest;
import com.semester3.attendance.enums.AttendanceStatus;
import com.semester3.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ---- Clock in / clock out ----

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceResponse> clockIn(@Valid @RequestBody ClockInRequest request) {
        return new ResponseEntity<>(attendanceService.clockIn(request), HttpStatus.CREATED);
    }

    @PutMapping("/clock-out/{employeeId}")
    public ResponseEntity<AttendanceResponse> clockOut(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.clockOut(employeeId));
    }

    // ---- Admin / manual management ----

    @PostMapping
    public ResponseEntity<AttendanceResponse> createOrUpdateManually(@Valid @RequestBody AttendanceRequest request) {
        return new ResponseEntity<>(attendanceService.createOrUpdateManually(request), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AttendanceResponse> updateStatus(@PathVariable Long id,
                                                             @RequestParam AttendanceStatus status) {
        return ResponseEntity.ok(attendanceService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attendanceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Reads ----

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getAll() {
        return ResponseEntity.ok(attendanceService.getAll());
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceResponse>> getByEmployeeId(@PathVariable Long employeeId) {
        return ResponseEntity.ok(attendanceService.getByEmployeeId(employeeId));
    }

    @GetMapping("/employee/{employeeId}/date/{date}")
    public ResponseEntity<AttendanceResponse> getByEmployeeIdAndDate(
            @PathVariable Long employeeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getByEmployeeIdAndDate(employeeId, date));
    }

    @GetMapping("/employee/{employeeId}/range")
    public ResponseEntity<List<AttendanceResponse>> getByEmployeeIdAndDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getByEmployeeIdAndDateRange(employeeId, startDate, endDate));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<AttendanceResponse>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getByDate(date));
    }
}
