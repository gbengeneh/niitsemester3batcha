package com.semester3.organization.controller;

import com.semester3.organization.dto.OrganizationDto;
import com.semester3.organization.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public OrganizationDto createOrganization(@Valid @RequestBody OrganizationDto organizationDto) {
        return organizationService.createOrganization(organizationDto);
    }

    @GetMapping("/{id}")
    public OrganizationDto getOrganizationById(@PathVariable Long id) {
        return organizationService.getOrganizationById(id);
    }

    @GetMapping
    public List<OrganizationDto> getAllOrganizations() {
        return organizationService.getAllOrganizations();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public OrganizationDto updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationDto organizationDto
    ) {
        return organizationService.updateOrganization(id, organizationDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return "Organization deleted successfully";
    }
}