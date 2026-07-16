package com.semester3.employee_service.utility;

import com.semester3.employee_service.dto.EmployeeDTO;
import com.semester3.employee_service.entity.Employee;

public class EmployeeMapper {

    public static EmployeeDTO toDTO(Employee e){
        return  new EmployeeDTO(
                e.getId(),
                e.getName(),
                e.getPosition(),
                e.getDepartmentId(),
                e.getOrganizationId(),
                e.getSalary()
        );
    }


    public static Employee toEntity(EmployeeDTO dto){
        return  new Employee(
                dto.getId(),
                dto.getName(),
                dto.getPosition(),
                dto.getDepartmentId(),
                dto.getOrganizationId(),
                dto.getSalary()
        );
    }
}
