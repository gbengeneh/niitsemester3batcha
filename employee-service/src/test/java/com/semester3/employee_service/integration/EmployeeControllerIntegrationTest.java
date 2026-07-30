package com.semester3.employee_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.semester3.employee_service.client.DirectoryClient;
import com.semester3.employee_service.dto.EmployeeDTO;
import com.semester3.employee_service.entity.Employee;
import com.semester3.employee_service.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository repository;

    @MockitoBean
    private DirectoryClient directoryClient;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_asAdmin_persistsAndReturnsEmployee() throws Exception {
        EmployeeDTO request = new EmployeeDTO(
                null, "Ada Lovelace", "Software Engineer", 10L, 20L, "500000"
        );

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Ada Lovelace"))
                .andExpect(jsonPath("$.departmentId").value(10))
                .andExpect(jsonPath("$.organizationId").value(20));

        verify(directoryClient).getDepartment(10L);
        verify(directoryClient).getOrganization(20L);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void createEmployee_withoutAdminRole_returnsForbidden() throws Exception {
        EmployeeDTO request = new EmployeeDTO(
                null, "Ada Lovelace", "Software Engineer", 10L, 20L, "500000"
        );

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createEmployee_withInvalidBody_returnsBadRequest() throws Exception {
        EmployeeDTO invalidRequest = new EmployeeDTO(
                null, "", "Software Engineer", null, null, "500000"
        );

        mockMvc.perform(post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getEmployee_whenStored_returnsEmployee() throws Exception {
        Employee saved = repository.save(new Employee(
                null, "Grace Hopper", "Team Lead", 11L, 21L, "650000"
        ));

        mockMvc.perform(get("/api/v1/employees/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Grace Hopper"))
                .andExpect(jsonPath("$.position").value("Team Lead"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getEmployee_whenMissing_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/employees/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    void employeeExists_isPublicAndReturnsFalseForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/employees/{id}/exists", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
}
