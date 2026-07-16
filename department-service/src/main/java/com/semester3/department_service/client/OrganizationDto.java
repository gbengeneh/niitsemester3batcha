package com.semester3.department_service.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Trimmed-down mirror of organization's OrganizationDto - only the fields
// department-service actually needs when validating an organization reference.
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrganizationDto {
    private Long id;
    private String name;
}
