package com.semester3.payroll_services.dto;

import java.math.BigDecimal;

public record PayrollResponse(
        Long id,
        Long employeeId,
        BigDecimal basicSalary,
        BigDecimal allowance,
        BigDecimal bonus,
        BigDecimal tax,
        BigDecimal netSalary,
        String payPeriod
) {}
