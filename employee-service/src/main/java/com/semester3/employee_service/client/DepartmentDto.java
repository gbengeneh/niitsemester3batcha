package com.semester3.employee_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Trimmed-down mirror of department-service's DepartmentDto - only the fields
// employee-service actually needs when validating/displaying a department.
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DepartmentDto {
    private Long id;
    private String departmentName;
}
