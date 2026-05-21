package com.semester3.user_service.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
