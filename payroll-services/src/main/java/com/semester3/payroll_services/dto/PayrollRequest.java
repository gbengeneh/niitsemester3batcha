package com.semester3.payroll_services.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record PayrollRequest (
    @NotNull Long employeeId,
    @NotNull @DecimalMin("0.0") BigDecimal basicSalary,
    @DecimalMin("0.0") BigDecimal allowance,
    @DecimalMin("0.0") BigDecimal bonus,
    @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}") String payPeriod
) {}
