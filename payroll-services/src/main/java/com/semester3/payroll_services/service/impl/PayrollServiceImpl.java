package com.semester3.payroll_services.service.impl;

import com.semester3.payroll_services.client.EmployeeClient;
import com.semester3.payroll_services.dto.PayrollRequest;
import com.semester3.payroll_services.dto.PayrollUpdateRequest;
import com.semester3.payroll_services.dto.PayrollResponse;
import com.semester3.payroll_services.entity.Payroll;
import com.semester3.payroll_services.exception.DuplicatePayrollException;
import com.semester3.payroll_services.exception.EmployeeNotFoundException;
import com.semester3.payroll_services.exception.PayrollNotFoundException;
import com.semester3.payroll_services.mapper.PayrollMapper;
import com.semester3.payroll_services.repository.PayrollRepository;
import com.semester3.payroll_services.service.PayrollService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final EmployeeClient employeeClient;
    private final PayrollMapper mapper;

    // Example flat rate — replace with a progressive bracket calculator
    // once tax rules for your jurisdiction are finalized.
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    @Override
    public PayrollResponse createPayroll(PayrollRequest request) {
        validateEmployeeExists(request.employeeId());

        if (payrollRepository.existsByEmployeeIdAndPayPeriod(request.employeeId(), request.payPeriod())) {
            throw new DuplicatePayrollException(request.employeeId(), request.payPeriod());
        }

        BigDecimal allowance = defaultZero(request.allowance());
        BigDecimal bonus = defaultZero(request.bonus());
        BigDecimal gross = request.basicSalary().add(allowance).add(bonus);
        BigDecimal tax = calculateTax(gross);
        BigDecimal net = gross.subtract(tax);

        Payroll payroll = Payroll.builder()
            .employeeId(request.employeeId())
            .basicSalary(request.basicSalary())
            .allowance(allowance)
            .bonus(bonus)
            .tax(tax)
            .netSalary(net)
            .payPeriod(request.payPeriod())
            .build();

        return mapper.toResponse(payrollRepository.save(payroll));
    }

    @Override
    public List<PayrollResponse> getByEmployeeId(Long employeeId) {
        return mapper.toResponseList(payrollRepository.findByEmployeeId(employeeId));
    }

    @Override
    public PayrollResponse getPayslip(Long employeeId, String payPeriod) {
        Payroll payroll = payrollRepository.findByEmployeeIdAndPayPeriod(employeeId, payPeriod)
            .orElseThrow(() -> new PayrollNotFoundException(employeeId, payPeriod));
        return mapper.toResponse(payroll);
    }

    @Override
    public PayrollResponse updatePayroll(Long employeeId, String payPeriod, PayrollUpdateRequest request) {
        Payroll payroll = payrollRepository.findByEmployeeIdAndPayPeriod(employeeId, payPeriod)
            .orElseThrow(() -> new PayrollNotFoundException(employeeId, payPeriod));

        if (request.basicSalary() != null) {
            payroll.setBasicSalary(request.basicSalary());
        }
        if (request.allowance() != null) {
            payroll.setAllowance(request.allowance());
        }
        if (request.bonus() != null) {
            payroll.setBonus(request.bonus());
        }

        BigDecimal gross = payroll.getBasicSalary().add(payroll.getAllowance()).add(payroll.getBonus());
        BigDecimal tax = calculateTax(gross);
        payroll.setTax(tax);
        payroll.setNetSalary(gross.subtract(tax));

        return mapper.toResponse(payrollRepository.save(payroll));
    }

    private BigDecimal calculateTax(BigDecimal gross) {
        return gross.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultZero(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val;
    }

    private void validateEmployeeExists(Long employeeId) {
        Boolean exists;
        try {
            exists = employeeClient.employeeExists(employeeId).getBody();
        } catch (FeignException.NotFound e) {
            throw new EmployeeNotFoundException(employeeId);
        }
        if (Boolean.FALSE.equals(exists)) {
            throw new EmployeeNotFoundException(employeeId);
        }
    }
}
