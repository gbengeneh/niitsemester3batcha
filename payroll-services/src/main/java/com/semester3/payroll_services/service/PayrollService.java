package com.semester3.payroll_services.service;

import com.semester3.payroll_services.dto.PayrollRequest;
import com.semester3.payroll_services.dto.PayrollUpdateRequest;
import com.semester3.payroll_services.dto.PayrollResponse;

import java.util.List;

public interface PayrollService {

    PayrollResponse createPayroll(PayrollRequest request);

    List<PayrollResponse> getByEmployeeId(Long employeeId);

    PayrollResponse getPayslip(Long employeeId, String payPeriod);

    PayrollResponse updatePayroll(Long employeeId, String payPeriod, PayrollUpdateRequest request);
}
