package com.semester3.payroll_services.client;

import com.semester3.payroll_services.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Only coupling point to Employee Service: a boolean existence check.
 * Employee Service never sends salary data here, and this service never
 * sends salary data there.
 */
@FeignClient(
    name = "employee-service",
    configuration = FeignConfig.class
)
public interface EmployeeClient {

    @GetMapping("/api/v1/employees/{id}/exists")
    ResponseEntity<Boolean> employeeExists(@PathVariable("id") Long id);
}
