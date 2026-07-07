package com.semester3.organization.service;

import com.semester3.organization.dto.OrganizationDto;
import com.semester3.organization.entity.Organization;
import com.semester3.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Override
    public OrganizationDto createOrganization(OrganizationDto organizationDto) {
        Organization organization = mapToEntity(organizationDto);
        Organization savedOrganization = organizationRepository.save(organization);
        return mapToDto(savedOrganization);
    }

    @Override
    public OrganizationDto getOrganizationById(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found with id: " + id));

        return mapToDto(organization);
    }

    @Override
    public List<OrganizationDto> getAllOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public OrganizationDto updateOrganization(Long id, OrganizationDto organizationDto) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found with id: " + id));

        organization.setName(organizationDto.getName());
        organization.setAddress(organizationDto.getAddress());
        organization.setEmail(organizationDto.getEmail());
        organization.setPhone(organizationDto.getPhone());
        organization.setWebsite(organizationDto.getWebsite());

        Organization updatedOrganization = organizationRepository.save(organization);
        return mapToDto(updatedOrganization);
    }

    @Override
    public void deleteOrganization(Long id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organization not found with id: " + id));

        organizationRepository.delete(organization);
    }

    private OrganizationDto mapToDto(Organization organization) {
        return OrganizationDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .address(organization.getAddress())
                .email(organization.getEmail())
                .phone(organization.getPhone())
                .website(organization.getWebsite())
                .build();
    }

    private Organization mapToEntity(OrganizationDto organizationDto) {
        return Organization.builder()
                .name(organizationDto.getName())
                .address(organizationDto.getAddress())
                .email(organizationDto.getEmail())
                .phone(organizationDto.getPhone())
                .website(organizationDto.getWebsite())
                .build();
    }
}