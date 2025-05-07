package com.demoApp.authentication.service.impl;

import com.demoApp.authentication.dto.*;
import com.demoApp.authentication.entity.Authentication;
import com.demoApp.authentication.entity.Authentication.Role;
import com.demoApp.authentication.repository.AuthenticationRepository;
import com.demoApp.authentication.security.JwtTokenUtil;
import com.demoApp.authentication.service.AuthenticationService;
import com.demoApp.authentication.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImplTemp implements AuthenticationService {

    private final AuthenticationRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final EmailService emailService;
    
    @Value("${app.account.max-failed-attempts:5}")
    private int maxFailedAttempts;
    
    @Value("${app.account.lock-time-minutes:30}")
    private int lockTimeInMinutes;

    @Override
    @Transactional
    public AuthResponse login(AuthRequest authRequest) {
        log.info("Attempting login for user: {}", authRequest.getUsername());
        
        Optional<Authentication> authOpt = authRepository.findByUsername(authRequest.getUsername());
        
        if (authOpt.isEmpty()) {
            log.warn("Login failed: User not found - {}", authRequest.getUsername());
            return AuthResponse.builder()
                    .status("FAILED")
                    .message("Invalid username or password")
                    .build();
        }
        
        Authentication auth = authOpt.get();
        
        if (auth.isAccountLocked()) {
            log.warn("Login failed: Account locked - {}", authRequest.getUsername());
            return AuthResponse.builder()
                    .status("FAILED")
                    .message("Your account is locked due to too many failed attempts. Please try again later or reset your password.")
                    .build();
        }
        
        if (!passwordEncoder.matches(authRequest.getPassword(), auth.getPassword())) {
            auth.incrementFailedAttempts();
            if (auth.getFailedAttempts() >= maxFailedAttempts) {
                auth.lock(lockTimeInMinutes);
                log.warn("Account locked for user: {} due to {} failed attempts", 
                        auth.getUsername(), auth.getFailedAttempts());
            }
            authRepository.save(auth);
            return failedLoginResponse(authRequest.getUsername());
        }
        
        if (!auth.isEnabled()) {
            log.warn("Login failed: Account disabled - {}", authRequest.getUsername());
            return AuthResponse.builder()
                    .status("FAILED")
                    .message("Your account is disabled")
                    .build();
        }
        
        auth.resetFailedAttempts();
        String rolesString = String.join(",", 
            auth.getRoles() != null ? auth.getRoles() : Set.of(Role.USER.toString()));
        String token = jwtTokenUtil.generateToken(auth.getUsername(), auth.getUserId(), rolesString);
        
        auth.setLastLogin(LocalDateTime.now());
        auth.setToken(token);
        authRepository.save(auth);
        
        if (auth.isMfaEnabled()) {
            return AuthResponse.builder()
                .username(auth.getUsername())
                .status("MFA_REQUIRED")
                .message("MFA verification required")
                .build();
        }
        
        return buildAuthResponse(auth, token, rolesString, "SUCCESS", "Login successful");
    }

    private AuthResponse failedLoginResponse(String username) {
        return AuthResponse.builder()
                .status("FAILED")
                .message("Invalid username or password")
                .build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (authRepository.existsByUsername(registerRequest.getUsername())) {
            return AuthResponse.builder()
                    .status("FAILED")
                    .message("Username already exists")
                    .build();
        }
        
        Authentication auth = new Authentication();
        auth.setUsername(registerRequest.getUsername());
        auth.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        auth.setEnabled(true);
        auth.setCreatedAt(LocalDateTime.now());
        auth.setRoles(Set.of(
            (registerRequest.getRole() != null ? registerRequest.getRole() : Role.USER).toString()));
        
        Authentication savedAuth = authRepository.save(auth);
        String rolesString = String.join(",", savedAuth.getRoles());
        String token = jwtTokenUtil.generateToken(
            savedAuth.getUsername(), 
            savedAuth.getUserId(), 
            rolesString);
        
        savedAuth.setToken(token);
        authRepository.save(savedAuth);
        
        return buildAuthResponse(savedAuth, token, rolesString, "SUCCESS", "Registration successful");
    }

    @Override
    public boolean validateToken(String token) {
        return StringUtils.hasText(token) && jwtTokenUtil.validateToken(token);
    }

    @Override
    public MfaSetupResponse setupMfa(String username) {
        log.info("MFA setup is temporarily disabled");
        return MfaSetupResponse.builder()
                .message("MFA setup is temporarily disabled")
                .build();
    }

    @Override
    public boolean verifyMfa(MfaVerifyRequest request) {
        log.info("MFA verification is temporarily disabled - auto-approving");
        return true;
    }

    @Override
    public boolean disableMfa(String username) {
        log.info("MFA disable is temporarily disabled - auto-approving");
        return true;
    }

    @Override
    public AuthResponse refreshToken(String token) {
        if (!validateToken(token)) {
            return AuthResponse.builder()
                    .status("FAILED")
                    .message("Invalid or expired token")
                    .build();
        }

        String username = jwtTokenUtil.getUsernameFromToken(token);
        String rolesString = jwtTokenUtil.getRoleFromToken(token);
        String newToken = jwtTokenUtil.generateToken(username, jwtTokenUtil.getUserIdFromToken(token), rolesString);
        
        Authentication auth = authRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        auth.setToken(newToken);
        authRepository.save(auth);
        
        return buildAuthResponse(auth, newToken, rolesString, "SUCCESS", "Token refreshed successfully");
    }

    @Override
    public void logout(String token) {
        log.info("User logged out");
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        return authRepository.findByUserId(userId)
            .filter(auth -> passwordEncoder.matches(oldPassword, auth.getPassword()))
            .map(auth -> {
                auth.setPassword(passwordEncoder.encode(newPassword));
                auth.setToken(null);
                authRepository.save(auth);
                return true;
            })
            .orElse(false);
    }

    @Override
    public boolean requestPasswordReset(PasswordResetRequest request) {
        return authRepository.findByUsername(request.getUsername())
            .map(auth -> {
                String resetToken = UUID.randomUUID().toString();
                auth.setResetToken(resetToken);
                auth.setResetTokenExpiry(LocalDateTime.now().plusHours(24));
                authRepository.save(auth);
                emailService.sendPasswordResetEmail(request.getEmail(), 
                    "https://yourdomain.com/reset-password?token=" + resetToken);
                return true;
            })
            .orElse(false);
    }

    @Override
    public boolean confirmPasswordReset(PasswordResetConfirmRequest request) {
        if (!StringUtils.hasText(request.getToken())) return false;
        if (!request.getNewPassword().equals(request.getConfirmPassword())) return false;
        
        return authRepository.findByResetToken(request.getToken())
            .filter(auth -> auth.getResetTokenExpiry().isAfter(LocalDateTime.now()))
            .map(auth -> {
                auth.setPassword(passwordEncoder.encode(request.getNewPassword()));
                auth.setResetToken(null);
                auth.setResetTokenExpiry(null);
                auth.resetFailedAttempts();
                auth.setToken(null);
                authRepository.save(auth);
                return true;
            })
            .orElse(false);
    }

    @Override
    public AuthResponse oauthLogin(OAuthLoginRequest request) {
        // Note: This is a placeholder implementation. You might want to add actual OAuth logic here
        Map<String, String> userInfo = validateOAuthToken(request.getProvider(), request.getAccessToken());
        if (userInfo == null || !userInfo.containsKey("id") || !userInfo.containsKey("email")) {
            return AuthResponse.builder()
                    .status("FAILED")
                    .message("Invalid OAuth token")
                    .build();
        }
        
        String email = userInfo.get("email");
        Authentication auth = authRepository.findByOauthProviderAndProviderId(request.getProvider(), userInfo.get("id"))
            .or(() -> authRepository.findByUsername(email))
            .map(existingAuth -> {
                existingAuth.setLastLogin(LocalDateTime.now());
                return existingAuth;
            })
            .orElseGet(() -> createNewOAuthUser(request.getProvider(), userInfo.get("id"), email));
        
        auth = authRepository.save(auth);
        String rolesString = String.join(",", auth.getRoles() != null ? auth.getRoles() : Set.of(Role.USER.toString()));
        String token = jwtTokenUtil.generateToken(auth.getUsername(), auth.getUserId(), rolesString);
        
        auth.setToken(token);
        authRepository.save(auth);
        
        return buildAuthResponse(auth, token, rolesString, "SUCCESS", "OAuth login successful");
    }

    private Authentication createNewOAuthUser(String provider, String providerId, String email) {
        Authentication auth = new Authentication();
        auth.setUsername(email);
        auth.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        auth.setEnabled(true);
        auth.setCreatedAt(LocalDateTime.now());
        auth.setRoles(Set.of(Role.USER.toString()));
        
        Set<Authentication.OAuthProvider> providers = new HashSet<>();
        providers.add(new Authentication.OAuthProvider(provider, providerId));
        auth.setOauthProviders(providers);
        
        return auth;
    }

    private Map<String, String> validateOAuthToken(String provider, String accessToken) {
        // Placeholder implementation
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("id", UUID.randomUUID().toString());
        userInfo.put("email", "user@example.com");
        return userInfo;
    }

    private void handleFailedLogin(Authentication auth) {
        auth.incrementFailedAttempts();
        if (auth.getFailedAttempts() >= maxFailedAttempts) {
            auth.lock(lockTimeInMinutes);
            log.warn("Account locked for user: {} due to {} failed attempts", 
                auth.getUsername(), auth.getFailedAttempts());
        }
        authRepository.save(auth);
    }

    @Override
    public AuthResponse authenticate(LoginRequest loginRequest) {
        Authentication auth = authRepository.findByUsername(loginRequest.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), auth.getPassword())) {
            handleFailedLogin(auth);
            throw new BadCredentialsException("Invalid password");
        }

        if (!auth.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }

        if (auth.isAccountLocked()) {
            throw new BadCredentialsException("Account is locked");
        }

        if (auth.isMfaEnabled()) {
            return AuthResponse.builder()
                .username(auth.getUsername())
                .status("MFA_REQUIRED")
                .message("MFA verification required")
                .build();
        }

        String rolesString = String.join(",", auth.getRoles());
        String token = jwtTokenUtil.generateToken(auth.getUsername(), auth.getUserId(), rolesString);

        return buildAuthResponse(auth, token, rolesString, "SUCCESS", "Login successful");
    }

    @Override
    public boolean addRole(Long userId, String role) {
        try {
            Role.valueOf(role);
            return authRepository.findByUserId(userId)
                .map(auth -> {
                    if (auth.getRoles() == null) auth.setRoles(new HashSet<>());
                    if (auth.getRoles().add(role)) {
                        authRepository.save(auth);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean removeRole(Long userId, String role) {
        return authRepository.findByUserId(userId)
            .filter(auth -> auth.getRoles() != null && auth.getRoles().remove(role))
            .map(auth -> {
                if (auth.getRoles().isEmpty()) auth.getRoles().add(Role.USER.toString());
                authRepository.save(auth);
                return true;
            })
            .orElse(false);
    }

    @Override
    public Set<String> getUserRoles(Long userId) {
        return authRepository.findByUserId(userId)
            .map(auth -> auth.getRoles() != null ? 
                new HashSet<>(auth.getRoles()) : Set.of(Role.USER.toString()))
            .orElse(Collections.emptySet());
    }

    @Override
    public boolean isAccountLocked(String username) {
        return authRepository.findByUsername(username)
            .map(Authentication::isAccountLocked)
            .orElse(false);
    }

    @Override
    public void unlockAccount(String username) {
        authRepository.findByUsername(username).ifPresent(auth -> {
            auth.resetFailedAttempts();
            authRepository.save(auth);
        });
    }

    public AuthResponse buildAuthResponse(Authentication auth, String token, String roles, String status, String message) {
        return AuthResponse.builder()
            .username(auth.getUsername())
            .role(roles)
            .status(status)
            .message(message)
            .accessToken(token)
            .mfaEnabled(auth.isMfaEnabled())
            .requiresMfaSetup(false)
            .expiresAt(LocalDateTime.now().plusDays(1))
            .build();
    }
}