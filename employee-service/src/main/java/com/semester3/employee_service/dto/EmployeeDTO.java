package com.semester3.employee_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmployeeDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String position;

    @NotNull(message = "Department id is required")
    private Long departmentId;

    @NotNull(message = "Organization id is required")
    private Long organizationId;

    private String salary;
}
