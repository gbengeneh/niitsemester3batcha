package com.semester3.employee_service;

import com.semester3.employee_service.dto.EmployeeDTO;
import com.semester3.employee_service.entity.Employee;
import com.semester3.employee_service.utility.EmployeeMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeMapperTest {

    @Test
    void toDTO_copiesAllEmployeeFields() {
        Employee employee = new Employee(
                1L, "Ada Lovelace", "Software Engineer", 10L, 20L, "500000"
        );

        EmployeeDTO result = EmployeeMapper.toDTO(employee);

        assertAll(
                () -> assertEquals(1L, result.getId()),
                () -> assertEquals("Ada Lovelace", result.getName()),
                () -> assertEquals("Software Engineer", result.getPosition()),
                () -> assertEquals(10L, result.getDepartmentId()),
                () -> assertEquals(20L, result.getOrganizationId()),
                () -> assertEquals("500000", result.getSalary())
        );
    }

    @Test
    void toEntity_copiesAllDTOFields() {
        EmployeeDTO dto = new EmployeeDTO(
                2L, "Grace Hopper", "Team Lead", 11L, 21L, "650000"
        );

        Employee result = EmployeeMapper.toEntity(dto);

        assertAll(
                () -> assertEquals(2L, result.getId()),
                () -> assertEquals("Grace Hopper", result.getName()),
                () -> assertEquals("Team Lead", result.getPosition()),
                () -> assertEquals(11L, result.getDepartmentId()),
                () -> assertEquals(21L, result.getOrganizationId()),
                () -> assertEquals("650000", result.getSalary())
        );
    }
}
