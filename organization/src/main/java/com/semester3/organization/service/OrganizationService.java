package com.semester3.organization.service;

import com.semester3.organization.dto.OrganizationDto;
import java.util.List;

public interface OrganizationService {

    OrganizationDto createOrganization(OrganizationDto organizationDto);

    OrganizationDto getOrganizationById(Long id);

    List<OrganizationDto> getAllOrganizations();

    OrganizationDto updateOrganization(Long id, OrganizationDto organizationDto);

    void deleteOrganization(Long id);
}