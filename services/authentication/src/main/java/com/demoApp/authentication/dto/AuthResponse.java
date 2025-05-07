package com.demoApp.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String status;
    private String message;
    private String username;
    private String role;
    private boolean mfaEnabled;
    private boolean requiresMfaSetup;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresAt;
} 