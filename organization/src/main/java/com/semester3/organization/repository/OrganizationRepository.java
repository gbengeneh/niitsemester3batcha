package com.semester3.organization.repository;

import com.semester3.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}