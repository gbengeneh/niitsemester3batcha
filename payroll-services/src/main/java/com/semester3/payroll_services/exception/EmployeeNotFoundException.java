package com.semester3.payroll_services.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(Long employeeId) {
        super(employeeId == null
            ? "Employee not found"
            : "Employee not found with id: " + employeeId);
    }
}
