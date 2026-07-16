package com.semester3.employee_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String position;

    // References department-service and organization respectively - validated
    // via a WebClient call before the employee is persisted (see EmployeeService).
    @NotNull(message = "Department id is required")
    private Long departmentId;

    @NotNull(message = "Organization id is required")
    private Long organizationId;

    private String salary;
}
