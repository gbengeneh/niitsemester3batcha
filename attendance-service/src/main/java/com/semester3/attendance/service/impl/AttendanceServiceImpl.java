package com.semester3.attendance.service.impl;

import com.semester3.attendance.dto.AttendanceRequest;
import com.semester3.attendance.dto.AttendanceResponse;
import com.semester3.attendance.dto.ClockInRequest;
import com.semester3.attendance.entity.Attendance;
import com.semester3.attendance.enums.AttendanceStatus;
import com.semester3.attendance.exception.DuplicateAttendanceException;
import com.semester3.attendance.exception.InvalidAttendanceOperationException;
import com.semester3.attendance.exception.ResourceNotFoundException;
import com.semester3.attendance.repository.AttendanceRepository;
import com.semester3.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;

    // Shift starts at 09:00. Clock-ins after 09:15 are marked LATE.
    // Tune these or move to application.yml / Config Server once you introduce it.
    private static final LocalTime SHIFT_START = LocalTime.of(9, 0);
    private static final LocalTime LATE_THRESHOLD = LocalTime.of(9, 15);
    private static final long HALF_DAY_MINUTES_THRESHOLD = 240; // less than 4 hours worked => HALF_DAY

    @Override
    public AttendanceResponse clockIn(ClockInRequest request) {
        LocalDate today = LocalDate.now();

        if (attendanceRepository.existsByEmployeeIdAndDate(request.getEmployeeId(), today)) {
            throw new DuplicateAttendanceException(
                    "Employee " + request.getEmployeeId() + " has already clocked in today");
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceStatus status = now.toLocalTime().isAfter(LATE_THRESHOLD)
                ? AttendanceStatus.LATE
                : AttendanceStatus.PRESENT;

        Attendance attendance = Attendance.builder()
                .employeeId(request.getEmployeeId())
                .date(today)
                .clockIn(now)
                .status(status)
                .build();

        return toResponse(attendanceRepository.save(attendance));
    }

    @Override
    public AttendanceResponse clockOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No clock-in record found for employee " + employeeId + " today"));

        if (attendance.getClockIn() == null) {
            throw new InvalidAttendanceOperationException("Cannot clock out before clocking in");
        }
        if (attendance.getClockOut() != null) {
            throw new InvalidAttendanceOperationException("Employee " + employeeId + " has already clocked out today");
        }

        LocalDateTime now = LocalDateTime.now();
        attendance.setClockOut(now);

        long workedMinutes = Duration.between(attendance.getClockIn(), now).toMinutes();
        if (workedMinutes < HALF_DAY_MINUTES_THRESHOLD && attendance.getStatus() != AttendanceStatus.LATE) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }

        return toResponse(attendanceRepository.save(attendance));
    }

    @Override
    public AttendanceResponse createOrUpdateManually(AttendanceRequest request) {
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(request.getEmployeeId(), request.getDate())
                .orElse(Attendance.builder()
                        .employeeId(request.getEmployeeId())
                        .date(request.getDate())
                        .build());

        attendance.setClockIn(request.getClockIn());
        attendance.setClockOut(request.getClockOut());
        attendance.setStatus(request.getStatus());

        return toResponse(attendanceRepository.save(attendance));
    }

    @Override
    public AttendanceResponse getById(Long id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Override
    public List<AttendanceResponse> getAll() {
        return attendanceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<AttendanceResponse> getByEmployeeId(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId).stream().map(this::toResponse).toList();
    }

    @Override
    public AttendanceResponse getByEmployeeIdAndDate(Long employeeId, LocalDate date) {
        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(employeeId, date)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No attendance record for employee " + employeeId + " on " + date));
        return toResponse(attendance);
    }

    @Override
    public List<AttendanceResponse> getByEmployeeIdAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<AttendanceResponse> getByDate(LocalDate date) {
        return attendanceRepository.findByDate(date).stream().map(this::toResponse).toList();
    }

    @Override
    public AttendanceResponse updateStatus(Long id, AttendanceStatus status) {
        Attendance attendance = findByIdOrThrow(id);
        attendance.setStatus(status);
        return toResponse(attendanceRepository.save(attendance));
    }

    @Override
    public void delete(Long id) {
        Attendance attendance = findByIdOrThrow(id);
        attendanceRepository.delete(attendance);
    }

    private Attendance findByIdOrThrow(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with id: " + id));
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        Long workedMinutes = null;
        if (attendance.getClockIn() != null && attendance.getClockOut() != null) {
            workedMinutes = Duration.between(attendance.getClockIn(), attendance.getClockOut()).toMinutes();
        }

        return AttendanceResponse.builder()
                .id(attendance.getId())
                .employeeId(attendance.getEmployeeId())
                .date(attendance.getDate())
                .clockIn(attendance.getClockIn())
                .clockOut(attendance.getClockOut())
                .status(attendance.getStatus())
                .workedMinutes(workedMinutes)
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .build();
    }
}
