package com.semester3.employee_service.client;

import com.semester3.employee_service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

// Validates that a department/organization actually exists before an Employee
// is allowed to reference it - the "best permissions" service-to-service call.
@Component
public class DirectoryClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.department-service.url}")
    private String departmentServiceUrl;

    @Value("${services.organization-service.url}")
    private String organizationServiceUrl;

    public DirectoryClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public DepartmentDto getDepartment(Long departmentId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(departmentServiceUrl + "/api/departments/" + departmentId)
                    .headers(this::forwardAuthorization)
                    .retrieve()
                    .bodyToMono(DepartmentDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("Department not found with id: " + departmentId);
        }
    }

    public OrganizationDto getOrganization(Long organizationId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(organizationServiceUrl + "/api/organizations/" + organizationId)
                    .headers(this::forwardAuthorization)
                    .retrieve()
                    .bodyToMono(OrganizationDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new ResourceNotFoundException("Organization not found with id: " + organizationId);
        }
    }

    // department-service and organization also require a valid JWT, so the
    // token on the incoming request is forwarded on the outbound call. There is
    // no gateway in this system to do this centrally, so each caller does it.
    private void forwardAuthorization(org.springframework.http.HttpHeaders headers) {
        Object requestAttrs = RequestContextHolder.getRequestAttributes();
        if (requestAttrs instanceof ServletRequestAttributes servletAttrs) {
            String authHeader = servletAttrs.getRequest().getHeader("Authorization");
            if (authHeader != null) {
                headers.set("Authorization", authHeader);
            }
        }
    }
}
