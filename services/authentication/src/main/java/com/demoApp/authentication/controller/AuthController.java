package com.demoApp.authentication.controller;

import com.demoApp.authentication.dto.*;
import com.demoApp.authentication.entity.Authentication;
import com.demoApp.authentication.entity.Authentication.Role;
import com.demoApp.authentication.exception.AuthenticationException;
import com.demoApp.authentication.repository.AuthenticationRepository;
import com.demoApp.authentication.security.JwtTokenUtil;
import com.demoApp.authentication.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthenticationService authService;
    private final AuthenticationRepository authRepository;
    private final JwtTokenUtil jwtTokenUtil;

    @Operation(summary = "User login", description = "Authenticate user credentials and return tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "202", description = "MFA verification required"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Login request received for username: {}", authRequest.getUsername());
        try {
            AuthResponse response = authService.login(authRequest);
            return switch (response.getStatus()) {
                case "SUCCESS" -> ResponseEntity.ok(response);
                case "MFA_REQUIRED" -> ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
                default -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            };
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .status("FAILED")
                            .message("An unexpected error occurred")
                            .build());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registration request received for username: {}", registerRequest.getUsername());
        try {
            AuthResponse response = authService.register(registerRequest);
            
            if ("SUCCESS".equals(response.getStatus())) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .status("FAILED")
                            .message("An unexpected error occurred")
                            .build());
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Token validation request received");
        try {
            String token = extractTokenFromHeader(authHeader);
            boolean isValid = authService.validateToken(token);
            return ResponseEntity.ok(Map.of("valid", isValid));
        } catch (AuthenticationException e) {
            return ResponseEntity.ok(Map.of("valid", false));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        log.info("Token refresh request received");
        try {
            String token = extractTokenFromHeader(authHeader);
            AuthResponse response = authService.refreshToken(token);
            
            if ("SUCCESS".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .status("FAILED")
                            .message(e.getMessage())
                            .build());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("Logout request received");
        try {
            String token = extractTokenFromHeader(authHeader);
            authService.logout(token);
            return ResponseEntity.noContent().build();
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        
        log.info("Change password request received");
        try {
            String token = extractTokenFromHeader(authHeader);
            Long userId = extractUserIdFromToken(token);
            
            boolean success = authService.changePassword(userId, oldPassword, newPassword);
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Failed to change password. Please check your current password."));
            }
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {
        
        log.info("Password reset request received for username: {}", request.getUsername());
        try {
            authService.requestPasswordReset(request);
            // Always return success to prevent user enumeration
            return ResponseEntity.ok(Map.of(
                    "message", "If your username and email are valid, you will receive a password reset link."));
        } catch (Exception e) {
            log.error("Password reset request failed", e);
            return ResponseEntity.ok(Map.of(
                    "message", "If your username and email are valid, you will receive a password reset link."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        
        log.info("Password reset confirmation received");
        try {
            boolean success = authService.confirmPasswordReset(request);
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Failed to reset password. The token may be invalid or expired."));
            }
        } catch (Exception e) {
            log.error("Password reset confirmation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An unexpected error occurred"));
        }
    }

    @PostMapping("/mfa/setup")
    public ResponseEntity<MfaSetupResponse> setupMfa(@RequestHeader("Authorization") String authHeader) {
        log.info("MFA setup request received");
        try {
            String token = extractTokenFromHeader(authHeader);
            String username = extractUsernameFromToken(token);
            
            MfaSetupResponse response = authService.setupMfa(username);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            log.error("MFA setup failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(MfaSetupResponse.builder()
                            .mfaEnabled(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("MFA setup failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MfaSetupResponse.builder()
                            .mfaEnabled(false)
                            .message("An error occurred during MFA setup")
                            .build());
        }
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<AuthResponse> verifyMfa(@Valid @RequestBody MfaVerifyRequest request) {
        log.info("MFA verification request received for username: {}", request.getUsername());
        try {
            boolean isValid = authService.verifyMfa(request);
            
            if (isValid) {
                Optional<Authentication> authOpt = authRepository.findByUsername(request.getUsername());
                if (authOpt.isPresent()) {
                    Authentication auth = authOpt.get();
                    String rolesString = auth.getRoles() != null && !auth.getRoles().isEmpty() 
                            ? String.join(",", auth.getRoles()) 
                            : Role.USER.toString();
                    
                    String token = jwtTokenUtil.generateToken(auth.getUsername(), auth.getUserId(), rolesString);
                    
                    auth.setLastLogin(LocalDateTime.now());
                    auth.setToken(token);
                    auth.resetFailedAttempts();
                    authRepository.save(auth);
                    
                    return ResponseEntity.ok(authService.buildAuthResponse(auth, token, rolesString, "SUCCESS", "MFA verification successful"));
                }
            }
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder()
                            .status("FAILED")
                            .message("Invalid MFA code")
                            .build());
        } catch (Exception e) {
            log.error("MFA verification failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .status("FAILED")
                            .message("An unexpected error occurred")
                            .build());
        }
    }

    @PostMapping("/mfa/disable")
    public ResponseEntity<Map<String, String>> disableMfa(@RequestHeader("Authorization") String authHeader) {
        log.info("MFA disable request received");
        try {
            String token = extractTokenFromHeader(authHeader);
            String username = extractUsernameFromToken(token);
            
            boolean success = authService.disableMfa(username);
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "MFA has been disabled"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Failed to disable MFA"));
            }
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/oauth/login")
    public ResponseEntity<AuthResponse> oauthLogin(@Valid @RequestBody OAuthLoginRequest request) {
        log.info("OAuth login request received for provider: {}", request.getProvider());
        try {
            AuthResponse response = authService.oauthLogin(request);
            
            if ("SUCCESS".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            log.error("OAuth login failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .status("FAILED")
                            .message("An unexpected error occurred")
                            .build());
        }
    }

    @Operation(summary = "Add role to user", description = "Add a role to a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/roles/{userId}/add")
    public ResponseEntity<RoleResponse> addRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        log.info("Add role request received for user ID: {} and role: {}", userId, role);
        try {
            boolean success = authService.addRole(userId, role);
            Set<String> updatedRoles = authService.getUserRoles(userId);
            
            return ResponseEntity.ok(RoleResponse.builder()
                    .status(success ? "SUCCESS" : "FAILED")
                    .success(success)
                    .userId(userId)
                    .roles(updatedRoles)
                    .message(success ? "Role added successfully" : "Failed to add role")
                    .build());
        } catch (Exception e) {
            log.error("Add role failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(RoleResponse.builder()
                            .status("FAILED")
                            .success(false)
                            .userId(userId)
                            .message("An unexpected error occurred")
                            .build());
        }
    }

    @PostMapping("/roles/{userId}/remove")
    public ResponseEntity<Map<String, String>> removeRole(
            @PathVariable Long userId,
            @RequestParam String role) {
        
        log.info("Remove role request received for user ID: {} and role: {}", userId, role);
        try {
            boolean success = authService.removeRole(userId, role);
            
            if (success) {
                return ResponseEntity.ok(Map.of("message", "Role removed successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Failed to remove role"));
            }
        } catch (Exception e) {
            log.error("Remove role failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An unexpected error occurred"));
        }
    }

    @GetMapping("/roles/{userId}")
    public ResponseEntity<Set<String>> getUserRoles(@PathVariable Long userId) {
        log.info("Get roles request received for user ID: {}", userId);
        try {
            Set<String> roles = authService.getUserRoles(userId);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            log.error("Get roles failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get account lock status", description = "Check if a user account is locked")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/account/lock-status")
    public ResponseEntity<AccountResponse> isAccountLocked(@RequestParam String username) {
        log.info("Account lock status check for username: {}", username);
        try {
            boolean locked = authService.isAccountLocked(username);
            return ResponseEntity.ok(AccountResponse.builder()
                    .status("SUCCESS")
                    .accountLocked(locked)
                    .username(username)
                    .success(true)
                    .message(locked ? "Account is locked" : "Account is not locked")
                    .build());
        } catch (Exception e) {
            log.error("Account lock status check failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AccountResponse.builder()
                            .status("FAILED")
                            .success(false)
                            .message("Failed to check account status")
                            .build());
        }
    }

    @Operation(summary = "Unlock account", description = "Unlock a locked user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account unlocked successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/account/unlock")
    public ResponseEntity<AccountResponse> unlockAccount(@RequestParam String username) {
        log.info("Account unlock request for username: {}", username);
        try {
            authService.unlockAccount(username);
            return ResponseEntity.ok(AccountResponse.builder()
                    .status("SUCCESS")
                    .success(true)
                    .username(username)
                    .accountLocked(false)
                    .message("Account unlocked successfully")
                    .build());
        } catch (Exception e) {
            log.error("Account unlock failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AccountResponse.builder()
                            .status("FAILED")
                            .success(false)
                            .message("Failed to unlock account")
                            .build());
        }
    }

    private Long extractUserIdFromToken(String token) {
        try {
            return jwtTokenUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.error("Error extracting user ID from token", e);
            throw new AuthenticationException("Invalid token");
        }
    }

    private String extractUsernameFromToken(String token) {
        try {
            return jwtTokenUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            log.error("Error extracting username from token", e);
            throw new AuthenticationException("Invalid token");
        }
    }

    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationException("Invalid authorization header");
        }
        return authHeader.substring(7);
    }
}