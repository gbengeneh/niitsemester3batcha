package com.semester3.payroll_services.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/**
 * Used for PATCH/PUT on an existing payroll record.
 * employeeId and payPeriod are deliberately excluded — those identify the
 * record and should never change via update; create a new record instead.
 */
public record PayrollUpdateRequest(

    @DecimalMin(value = "0.0", message = "basicSalary cannot be negative")
    BigDecimal basicSalary,

    @DecimalMin(value = "0.0", message = "allowance cannot be negative")
    BigDecimal allowance,

    @DecimalMin(value = "0.0", message = "bonus cannot be negative")
    BigDecimal bonus

) {}
