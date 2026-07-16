package com.semester3.employee_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Trimmed-down mirror of organization's OrganizationDto - only the fields
// employee-service actually needs when validating/displaying an organization.
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrganizationDto {
    private Long id;
    private String name;
}
