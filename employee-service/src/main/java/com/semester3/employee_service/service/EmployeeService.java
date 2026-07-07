package com.semester3.employee_service.service;

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

    public EmployeeService(EmployeeRepository repository){
        this.repository=repository;
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
        Employee employee = EmployeeMapper.toEntity(dto);
        return EmployeeMapper.toDTO(repository.save(employee));
    }

    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto){
        Employee existing = repository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Employee not found"));
        existing.setName(dto.getName());
        existing.setPosition(dto.getPosition());
        existing.setDepartment(dto.getDepartment());
        existing.setSalary(dto.getSalary());
        return EmployeeMapper.toDTO(repository.save(existing));
    }

    public void deleteEmployee(Long id){
        repository.deleteById(id);
    }
}
