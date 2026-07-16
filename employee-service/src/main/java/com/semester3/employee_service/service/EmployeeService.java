package com.semester3.employee_service.service;

import com.semester3.employee_service.client.DirectoryClient;
import com.semester3.employee_service.dto.EmployeeDTO;
import com.semester3.employee_service.entity.Employee;
import com.semester3.employee_service.exception.ResourceNotFoundException;
import com.semester3.employee_service.repository.EmployeeRepository;
import com.semester3.employee_service.utility.EmployeeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository repository;
    private final DirectoryClient directoryClient;

    public EmployeeService(EmployeeRepository repository, DirectoryClient directoryClient){
        this.repository=repository;
        this.directoryClient = directoryClient;
    }

     public List<EmployeeDTO> getAllEmployees(){
         return  repository.findAll().stream()
                 .map(EmployeeMapper::toDTO)
                 .toList();
    }

    public EmployeeDTO getEmployeeById(Long id){
        Employee employee = repository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Employee not found"));
        return EmployeeMapper.toDTO(employee);
    }

    public EmployeeDTO createEmployee(EmployeeDTO dto){
        directoryClient.getDepartment(dto.getDepartmentId());
        directoryClient.getOrganization(dto.getOrganizationId());
        Employee employee = EmployeeMapper.toEntity(dto);
        return EmployeeMapper.toDTO(repository.save(employee));
    }

    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto){
        Employee existing = repository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Employee not found"));
        directoryClient.getDepartment(dto.getDepartmentId());
        directoryClient.getOrganization(dto.getOrganizationId());
        existing.setName(dto.getName());
        existing.setPosition(dto.getPosition());
        existing.setDepartmentId(dto.getDepartmentId());
        existing.setOrganizationId(dto.getOrganizationId());
        existing.setSalary(dto.getSalary());
        return EmployeeMapper.toDTO(repository.save(existing));
    }

    public void deleteEmployee(Long id){
        repository.deleteById(id);
    }

    public boolean existsById(Long id){
        return repository.existsById(id);
    }
}
