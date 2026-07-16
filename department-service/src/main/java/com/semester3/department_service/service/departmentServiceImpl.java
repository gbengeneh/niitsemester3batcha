package com.semester3.department_service.service;

import com.semester3.department_service.client.DirectoryClient;
import com.semester3.department_service.dto.DepartmentDto;
import com.semester3.department_service.entity.Department;
import com.semester3.department_service.repository.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class departmentServiceImpl implements DepartmentService{

    private final DepartmentRepository departmentRepository;
    private final DirectoryClient directoryClient;

    public departmentServiceImpl(DepartmentRepository departmentRepository, DirectoryClient directoryClient) {
        this.departmentRepository = departmentRepository;
        this.directoryClient = directoryClient;
    }

    @Override
    public DepartmentDto createDepartment(DepartmentDto departmentDto){
        directoryClient.getOrganization(departmentDto.getOrganizationId());
        Department department= mapToEntity(departmentDto);
        Department savedDepartment = departmentRepository.save(department);
        return mapToDto(savedDepartment);
    }
    @Override
    public  DepartmentDto getDepartmentById(Long id){
        Department department = departmentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Department not found with is:" + id));
        return mapToDto(department);
    }

    @Override
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public DepartmentDto updateDepartment(Long id, DepartmentDto departmentDto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        directoryClient.getOrganization(departmentDto.getOrganizationId());

        department.setDepartmentName(departmentDto.getDepartmentName());
        department.setDepartmentCode(departmentDto.getDepartmentCode());
        department.setDepartmentAddress(departmentDto.getDepartmentAddress());
        department.setOrganizationId(departmentDto.getOrganizationId());

        Department updatedDepartment = departmentRepository.save(department);

        return mapToDto(updatedDepartment);
    }

    @Override
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));

        departmentRepository.delete(department);
    }

    private DepartmentDto mapToDto(Department department) {
        return DepartmentDto.builder()
                .id(department.getId())
                .departmentName(department.getDepartmentName())
                .departmentCode(department.getDepartmentCode())
                .departmentAddress(department.getDepartmentAddress())
                .organizationId(department.getOrganizationId())
                .build();
    }

    private Department mapToEntity(DepartmentDto departmentDto) {
        return Department.builder()
                .departmentName(departmentDto.getDepartmentName())
                .departmentCode(departmentDto.getDepartmentCode())
                .departmentAddress(departmentDto.getDepartmentAddress())
                .organizationId(departmentDto.getOrganizationId())
                .build();
    }

}
