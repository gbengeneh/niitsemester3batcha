package com.semester3.department_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

// Validates that an organization actually exists before a Department is
// allowed to reference it - the "best permissions" service-to-service call.
@Component
public class DirectoryClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.organization-service.url}")
    private String organizationServiceUrl;

    public DirectoryClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
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
            throw new RuntimeException("Organization not found with id: " + organizationId);
        }
    }

    // organization also requires a valid JWT, so the token on the incoming
    // request is forwarded on the outbound call. There is no gateway in this
    // system to do this centrally, so each caller does it.
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
