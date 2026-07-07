package com.semester3.department_service.controller;

import com.semester3.department_service.dto.DepartmentDto;
import com.semester3.department_service.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    public DepartmentDto createDepartment(@Valid @RequestBody DepartmentDto departmentDto) {
        return departmentService.createDepartment(departmentDto);
    }

    @GetMapping("/{id}")
    public DepartmentDto getDepartmentById(@PathVariable Long id) {
        return departmentService.getDepartmentById(id);
    }

    @GetMapping
    public List<DepartmentDto> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @PutMapping("/{id}")
    public DepartmentDto updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentDto departmentDto
    ) {
        return departmentService.updateDepartment(id, departmentDto);
    }

    @DeleteMapping("/{id}")
    public String deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return "Department deleted successfully";
    }

}
