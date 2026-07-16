package com.semester3.payroll_services.repository;

import com.semester3.payroll_services.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByEmployeeId(Long employeeId);

    Optional<Payroll> findByEmployeeIdAndPayPeriod(Long employeeId, String payPeriod);

    boolean existsByEmployeeIdAndPayPeriod(Long employeeId, String payPeriod);
}
