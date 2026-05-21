package com.semester3.user_service.dto;

import com.semester3.user_service.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthUserResponse {
     private Long id;
     private String username;
     private String email;
     private Set<Role> roles;
     private String accessToken;
     private String refreshToken;
}
