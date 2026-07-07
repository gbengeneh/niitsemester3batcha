package com.semester3.organization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDto {

    private Long id;

    @NotBlank(message = "Organization name is required")
    private String name;

    private String address;

    @Email(message = "Enter a valid email address")
    private String email;

    private String phone;
    private String website;
}