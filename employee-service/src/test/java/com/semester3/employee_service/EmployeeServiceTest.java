package com.semester3.employee_service;

import com.semester3.employee_service.client.DirectoryClient;
import com.semester3.employee_service.dto.EmployeeDTO;
import com.semester3.employee_service.entity.Employee;
import com.semester3.employee_service.exception.ResourceNotFoundException;
import com.semester3.employee_service.repository.EmployeeRepository;
import com.semester3.employee_service.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @Mock
    private DirectoryClient directoryClient;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee employee;
    private EmployeeDTO dto;

    @BeforeEach
    void setUp() {
        employee = new Employee(
                1L, "Ada Lovelace", "Software Engineer", 10L, 20L, "500000"
        );
        dto = new EmployeeDTO(
                null, "Ada Lovelace", "Software Engineer", 10L, 20L, "500000"
        );
    }

    @Test
    void getAllEmployees_returnsMappedDTOs() {
        when(repository.findAll()).thenReturn(List.of(employee));

        List<EmployeeDTO> result = employeeService.getAllEmployees();

        assertEquals(1, result.size());
        assertEquals("Ada Lovelace", result.getFirst().getName());
        verify(repository).findAll();
    }

    @Test
    void getEmployeeById_whenEmployeeExists_returnsDTO() {
        when(repository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeDTO result = employeeService.getEmployeeById(1L);

        assertEquals(1L, result.getId());
        assertEquals("Ada Lovelace", result.getName());
        verify(repository).findById(1L);
    }

    @Test
    void getEmployeeById_whenEmployeeIsMissing_throwsResourceNotFoundException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> employeeService.getEmployeeById(99L)
        );

        assertEquals("Employee not found", exception.getMessage());
        verifyNoInteractions(directoryClient);
    }

    @Test
    void createEmployee_validatesReferencesAndSavesEmployee() {
        when(repository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        EmployeeDTO result = employeeService.createEmployee(dto);

        assertEquals(1L, result.getId());
        assertEquals("Ada Lovelace", result.getName());
        verify(directoryClient).getDepartment(10L);
        verify(directoryClient).getOrganization(20L);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(repository).save(captor.capture());
        assertEquals("Software Engineer", captor.getValue().getPosition());
    }

    @Test
    void updateEmployee_updatesTheExistingEntity() {
        EmployeeDTO update = new EmployeeDTO(
                null, "Ada King", "Senior Engineer", 12L, 22L, "700000"
        );
        when(repository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.save(employee)).thenReturn(employee);

        EmployeeDTO result = employeeService.updateEmployee(1L, update);

        assertEquals(1L, result.getId());
        assertEquals("Ada King", result.getName());
        assertEquals("Senior Engineer", result.getPosition());
        assertEquals(12L, result.getDepartmentId());
        assertEquals(22L, result.getOrganizationId());
        assertEquals("700000", result.getSalary());
        verify(directoryClient).getDepartment(12L);
        verify(directoryClient).getOrganization(22L);
        verify(repository).save(employee);
    }

    @Test
    void deleteEmployee_delegatesToRepository() {
        employeeService.deleteEmployee(1L);

        verify(repository).deleteById(1L);
        verifyNoInteractions(directoryClient);
    }

    @Test
    void existsById_whenEmployeeExists_returnsTrue() {
        when(repository.existsById(1L)).thenReturn(true);

        boolean result = employeeService.existsById(1L);

        assertTrue(result);
        verify(repository).existsById(1L);
        verify(repository, never()).findById(1L);
    }
}
