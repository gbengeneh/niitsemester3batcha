package com.semester3.payroll_services.exception;

public class DuplicatePayrollException extends RuntimeException {
    public DuplicatePayrollException(Long employeeId, String payPeriod) {
        super("Payroll already exists for employeeId " + employeeId + " and payPeriod " + payPeriod);
    }
}
