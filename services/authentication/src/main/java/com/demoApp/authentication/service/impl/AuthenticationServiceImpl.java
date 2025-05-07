package com.demoApp.authentication.service.impl;

import com.demoApp.authentication.dto.*;
import com.demoApp.authentication.entity.Authentication;
import com.demoApp.authentication.entity.Authentication.Role;
import com.demoApp.authentication.exception.AuthenticationException;
import com.demoApp.authentication.repository.AuthenticationRepository;
import com.demoApp.authentication.security.JwtTokenUtil;
import com.demoApp.authentication.service.AuthenticationService;
import com.demoApp.authentication.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.*;
import dev.samstevens.totp.secret.SecretGenerator;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final EmailService emailService;
    private final SecretGenerator secretGenerator;
    private final CodeVerifier codeVerifier;
    private final QrGenerator qrGenerator;
    
    @Value("${app.mfa.issuer:DemoApp}") private String mfaIssuer;
    @Value("${app.account.max-failed-attempts:5}") private int maxFailedAttempts;
    @Value("${app.account.lock-time-minutes:30}") private int lockTimeInMinutes;

    @Override
    @Transactional
    public AuthResponse login(AuthRequest request) {
        return authRepository.findByUsername(request.getUsername())
            .map(auth -> handleLogin(auth, request))
            .orElseGet(() -> failedLoginResponse(request.getUsername(), "Invalid username or password"));
    }

    private AuthResponse handleLogin(Authentication auth, AuthRequest request) {
        if (auth.isAccountLocked()) {
            return failedLoginResponse(auth.getUsername(), "Account locked due to too many failed attempts");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), auth.getPassword())) {
            handleFailedLogin(auth);
            return failedLoginResponse(auth.getUsername(), "Invalid username or password");
        }
        
        if (!auth.isEnabled()) {
            return failedLoginResponse(auth.getUsername(), "Account disabled");
        }
        
        if (auth.isMfaEnabled()) {
            return AuthResponse.builder()
                .username(auth.getUsername())
                .status("MFA_REQUIRED")
                .message("MFA verification required")
                .build();
        }
        
        return successfulLogin(auth);
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

    private AuthResponse successfulLogin(Authentication auth) {
        auth.resetFailedAttempts();
        String roles = String.join(",", auth.getRoles() != null ? auth.getRoles() : Set.of(Role.USER.toString()));
        String token = jwtTokenUtil.generateToken(auth.getUsername(), auth.getUserId(), roles);
        
        auth.setLastLogin(LocalDateTime.now());
        auth.setToken(token);
        authRepository.save(auth);
        
        return buildAuthResponse(auth, token, roles, "SUCCESS", "Login successful");
    }

    private AuthResponse failedLoginResponse(String username, String message) {
        log.warn("Login failed: {} - {}", message, username);
        return AuthResponse.builder().status("FAILED").message(message).build();
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (authRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.builder()
                .status("FAILED").message("Username already exists").build();
        }
        
        Authentication auth = new Authentication();
        auth.setUsername(request.getUsername());
        auth.setPassword(passwordEncoder.encode(request.getPassword()));
        auth.setEnabled(true);
        auth.setCreatedAt(LocalDateTime.now());
        auth.setRoles(Set.of((request.getRole() != null ? request.getRole() : Role.USER).toString()));
        
        Authentication savedAuth = authRepository.save(auth);
        String roles = String.join(",", savedAuth.getRoles());
        String token = jwtTokenUtil.generateToken(savedAuth.getUsername(), savedAuth.getUserId(), roles);
        
        savedAuth.setToken(token);
        authRepository.save(savedAuth);
        
        return buildAuthResponse(savedAuth, token, roles, "SUCCESS", "Registration successful");
    }
        
    public AuthResponse buildAuthResponse(Authentication auth, String token, String roles, String status, String message) {
        return AuthResponse.builder()
            .username(auth.getUsername())
            .accessToken(token)
            .role(roles)
            .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenUtil.getExpirationDateFromToken(token).getTime() / 1000))
            .status(status)
            .message(message)
            .mfaEnabled(auth.isMfaEnabled())
            .requiresMfaSetup(false)
            .build();
    }

    @Override
    public boolean validateToken(String token) {
        if (!jwtTokenUtil.validateToken(token) || jwtTokenUtil.isTokenExpired(token)) {
            return false;
        }
        
        return authRepository.findByUsername(jwtTokenUtil.getUsernameFromToken(token))
            .map(auth -> auth.isEnabled() && token.equals(auth.getToken()))
            .orElse(false);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String token) {
        if (!validateToken(token)) {
            return AuthResponse.builder().status("FAILED").message("Invalid or expired token").build();
        }
        
        String username = jwtTokenUtil.getUsernameFromToken(token);
        String newToken = jwtTokenUtil.generateToken(
            username,
            jwtTokenUtil.getUserIdFromToken(token),
            jwtTokenUtil.getRoleFromToken(token)
        );
        
        authRepository.findByUsername(username).ifPresent(auth -> {
            auth.setToken(newToken);
            authRepository.save(auth);
        });
        
        return buildAuthResponse(
            authRepository.findByUsername(username).orElseThrow(),
            newToken,
            jwtTokenUtil.getRoleFromToken(token),
            "SUCCESS",
            "Token refreshed successfully"
        );
    }

    @Override
    @Transactional
    public void logout(String token) {
        if (token != null && validateToken(token)) {
            authRepository.findByUsername(jwtTokenUtil.getUsernameFromToken(token))
                .ifPresent(auth -> {
                    auth.setToken(null);
                authRepository.save(auth);
                });
        }
    }

    @Override
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
    public MfaSetupResponse setupMfa(String username) {
        return authRepository.findByUsername(username)
            .map(auth -> {
        String secretKey = secretGenerator.generate();
        auth.setMfaSecret(secretKey);
                auth.setMfaEnabled(false);
        authRepository.save(auth);
        
                try {
                    String qrCodeUrl = "data:image/png;base64," + Base64.getEncoder().encodeToString(
                        qrGenerator.generate(new QrData.Builder()
                            .label(username).secret(secretKey).issuer(mfaIssuer)
                            .algorithm(HashingAlgorithm.SHA1).digits(6).period(30).build()));
                    
                    return MfaSetupResponse.builder()
                        .secretKey(secretKey).qrCodeUrl(qrCodeUrl)
                        .mfaEnabled(false)
                        .message("MFA setup initialized").build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
            })
            .orElseThrow(() -> new AuthenticationException("User not found"));
    }
    
    @Override
    @Transactional
    public boolean verifyMfa(MfaVerifyRequest request) {
        return authRepository.findByUsername(request.getUsername())
            .filter(auth -> auth.getMfaSecret() != null)
            .map(auth -> {
                boolean isValid = codeVerifier.isValidCode(auth.getMfaSecret(), request.getCode());
                if (isValid && !auth.isMfaEnabled()) {
                auth.setMfaEnabled(true);
                authRepository.save(auth);
                }
                return isValid;
            })
            .orElse(false);
    }
    
    @Override
    @Transactional
    public boolean disableMfa(String username) {
        return authRepository.findByUsername(username)
            .map(auth -> {
        auth.setMfaEnabled(false);
        auth.setMfaSecret(null);
        authRepository.save(auth);
        return true;
            })
            .orElse(false);
    }
    
    @Override
    @Transactional
    public AuthResponse oauthLogin(OAuthLoginRequest request) {
        Map<String, String> userInfo = validateOAuthToken(request.getProvider().toLowerCase(), request.getAccessToken());
        if (userInfo == null || !userInfo.containsKey("id") || !userInfo.containsKey("email")) {
            return AuthResponse.builder().status("FAILED").message("Invalid OAuth token").build();
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
        String roles = String.join(",", auth.getRoles() != null ? auth.getRoles() : Set.of(Role.USER.toString()));
        String token = jwtTokenUtil.generateToken(auth.getUsername(), auth.getUserId(), roles);
        
        auth.setToken(token);
        authRepository.save(auth);
        
        return buildAuthResponse(auth, token, roles, "SUCCESS", "OAuth login successful");
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
    
    @Override
    @Transactional
    public boolean addRole(Long userId, String role) {
        try {
            Role.valueOf(role); // Validate role
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
    @Transactional
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
            .map(auth -> auth.getRoles() != null ? new HashSet<>(auth.getRoles()) : Set.of(Role.USER.toString()))
            .orElse(Collections.emptySet());
    }
    
    @Override
    public boolean isAccountLocked(String username) {
        return authRepository.findByUsername(username)
            .map(Authentication::isAccountLocked)
            .orElse(false);
    }
    
    @Override
    @Transactional
    public void unlockAccount(String username) {
        authRepository.findByUsername(username).ifPresent(auth -> {
            auth.resetFailedAttempts();
            authRepository.save(auth);
        });
    }
    
    private Map<String, String> validateOAuthToken(String provider, String accessToken) {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("id", UUID.randomUUID().toString());
        userInfo.put("email", "user@example.com");
        return userInfo;
    }

    @Override
    public AuthResponse authenticate(LoginRequest loginRequest) {
        return authRepository.findByUsername(loginRequest.getUsername())
            .map(auth -> {
                if (!passwordEncoder.matches(loginRequest.getPassword(), auth.getPassword())) {
                    handleFailedLogin(auth);
                    throw new AuthenticationException("Invalid credentials");
                }

                if (!auth.isEnabled()) {
                    throw new AuthenticationException("Account is disabled");
                }

                if (auth.isAccountLocked()) {
                    throw new AuthenticationException("Account is locked");
                }

                if (auth.isMfaEnabled()) {
                    return AuthResponse.builder()
                        .username(auth.getUsername())
                        .status("MFA_REQUIRED")
                        .message("MFA verification required")
                        .build();
                }

                return successfulLogin(auth);
            })
            .orElseThrow(() -> new AuthenticationException("User not found"));
    }
} 