package com.semester3.payroll_services.controller;

import com.semester3.payroll_services.dto.PayrollRequest;
import com.semester3.payroll_services.dto.PayrollUpdateRequest;
import com.semester3.payroll_services.dto.PayrollResponse;
import com.semester3.payroll_services.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<PayrollResponse> create(@Valid @RequestBody PayrollRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollService.createPayroll(request));
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'FINANCE') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<List<PayrollResponse>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollService.getByEmployeeId(employeeId));
    }

    @GetMapping("/{employeeId}/{payPeriod}")
    @PreAuthorize("hasAnyRole('HR_ADMIN', 'FINANCE') or #employeeId == authentication.principal.employeeId")
    public ResponseEntity<PayrollResponse> getPayslip(
            @PathVariable Long employeeId,
            @PathVariable String payPeriod) {
        return ResponseEntity.ok(payrollService.getPayslip(employeeId, payPeriod));
    }

    @PatchMapping("/{employeeId}/{payPeriod}")
    @PreAuthorize("hasRole('HR_ADMIN')")
    public ResponseEntity<PayrollResponse> update(
            @PathVariable Long employeeId,
            @PathVariable String payPeriod,
            @Valid @RequestBody PayrollUpdateRequest request) {
        return ResponseEntity.ok(payrollService.updatePayroll(employeeId, payPeriod, request));
    }
}
