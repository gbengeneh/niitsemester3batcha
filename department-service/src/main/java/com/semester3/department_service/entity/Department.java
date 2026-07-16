package com.semester3.department_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String departmentName;
    private String departmentCode;
    private String departmentAddress;

    // References organization - validated via a WebClient call before the
    // department is persisted (see DepartmentService).
    private Long organizationId;
}
