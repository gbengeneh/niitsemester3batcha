package com.semester3.payroll_services.exception;

public class PayrollNotFoundException extends RuntimeException {
    public PayrollNotFoundException(Long employeeId, String payPeriod) {
        super("Payroll record not found for employeeId " + employeeId + " and payPeriod " + payPeriod);
    }

    public PayrollNotFoundException(Long id) {
        super("Payroll record not found with id: " + id);
    }
}
