package com.demoApp.authentication.service;

import com.demoApp.authentication.dto.*;
import com.demoApp.authentication.entity.Authentication;

import java.util.Set;

public interface AuthenticationService {
    
    AuthResponse login(AuthRequest authRequest);
    
    AuthResponse register(RegisterRequest registerRequest);
    
    boolean validateToken(String token);
    
    AuthResponse refreshToken(String token);
    
    void logout(String token);
    
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    
    // Password reset functionality
    boolean requestPasswordReset(PasswordResetRequest request);
    
    boolean confirmPasswordReset(PasswordResetConfirmRequest request);
    
    // MFA functionality
    MfaSetupResponse setupMfa(String username);
    
    boolean verifyMfa(MfaVerifyRequest request);
    
    boolean disableMfa(String username);
    
    // OAuth functionality
    AuthResponse oauthLogin(OAuthLoginRequest request);
    
    // Role management
    boolean addRole(Long userId, String role);
    
    boolean removeRole(Long userId, String role);
    
    Set<String> getUserRoles(Long userId);
    
    // Account lockout functionality
    boolean isAccountLocked(String username);
    
    void unlockAccount(String username);
    
    AuthResponse authenticate(LoginRequest loginRequest);
    
    // Helper method for building consistent auth responses
    default AuthResponse buildAuthResponse(Authentication auth, String token, String roles, String status, String message) {
        return AuthResponse.builder()
            .username(auth.getUsername())
            .accessToken(token)
            .role(roles)
            .expiresAt(java.time.LocalDateTime.now().plusSeconds(
                new com.demoApp.authentication.security.JwtTokenUtil().getExpirationDateFromToken(token).getTime() / 1000))
            .status(status)
            .message(message)
            .build();
    }
} 