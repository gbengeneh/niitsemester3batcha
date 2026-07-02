package com.semester3.department_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDto {
    private Long id;
    @NotBlank(message = "Department name is required")
    private String departmentName;
    @NotBlank(message = "Department code is required")
    private String departmentCode;
    @NotBlank(message = "Department address is required")
    private String getDepartmentAddress;

}
