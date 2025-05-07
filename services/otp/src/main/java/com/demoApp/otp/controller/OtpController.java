package com.demoApp.otp.controller;

import com.demoApp.otp.dto.OtpRequestDto;
import com.demoApp.otp.dto.OtpResponseDto;
import com.demoApp.otp.dto.OtpVerificationDto;
import com.demoApp.otp.entity.OtpEntity;
import com.demoApp.otp.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    @Autowired
    private OtpService otpService;

    /**
     * Generate OTP for email verification
     * @param requestDto OTP request containing email and optional userId
     * @return OTP response with masked OTP and expiry time
     */
    @PostMapping("/generate/email")
    public ResponseEntity<OtpResponseDto> generateEmailOtp(@Valid @RequestBody OtpRequestDto requestDto) {
        OtpEntity otpEntity = otpService.generateEmailOtp(requestDto.getRecipient(), requestDto.getUserId());
        
        return ResponseEntity.ok(buildOtpResponse(otpEntity));
    }
    
    /**
     * Generate OTP for phone verification
     * @param requestDto OTP request containing phone number and optional userId
     * @return OTP response with masked OTP and expiry time
     */
    @PostMapping("/generate/phone")
    public ResponseEntity<OtpResponseDto> generatePhoneOtp(@Valid @RequestBody OtpRequestDto requestDto) {
        OtpEntity otpEntity = otpService.generatePhoneOtp(requestDto.getRecipient(), requestDto.getUserId());
        
        return ResponseEntity.ok(buildOtpResponse(otpEntity));
    }
    
    /**
     * Verify OTP
     * @param verificationDto OTP verification request
     * @return OTP verification result
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody OtpVerificationDto verificationDto) {
        try {
            // Check if recipient is provided
            if (verificationDto.getRecipient() == null || verificationDto.getRecipient().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new OtpResponseDto(
                                null,
                                "Recipient is required",
                                null,
                                false,
                                null
                        )
                );
            }
            
            // Check if OTP is provided
            if (verificationDto.getOtp() == null || verificationDto.getOtp().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new OtpResponseDto(
                                verificationDto.getRecipient(),
                                "OTP is required",
                                null,
                                false,
                                null
                        )
                );
            }
            
            boolean verified = otpService.verifyOtp(verificationDto.getRecipient(), verificationDto.getOtp());
            
            if (verified) {
                return ResponseEntity.ok().body(
                        new OtpResponseDto(
                                verificationDto.getRecipient(),
                                "Verification successful",
                                null,
                                true,
                                null
                        )
                );
            } else {
                // Get the latest OTP for more specific error message
                OtpEntity latestOtp = otpService.getLatestOtp(verificationDto.getRecipient());
                
                String errorMessage = "Invalid or expired OTP";
                if (latestOtp != null) {
                    if (latestOtp.isExpired()) {
                        errorMessage = "OTP has expired. Please request a new one.";
                    } else if (latestOtp.isMaxAttemptsExceeded()) {
                        errorMessage = "Maximum attempts exceeded. Please request a new OTP.";
                    } else if (latestOtp.isUsed()) {
                        errorMessage = "OTP has already been used. Please request a new one.";
                    }
                }
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new OtpResponseDto(
                                verificationDto.getRecipient(),
                                errorMessage,
                                null,
                                false,
                                null
                        )
                );
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new OtpResponseDto(
                            verificationDto.getRecipient(),
                            "Error verifying OTP: " + e.getMessage(),
                            null,
                            false,
                            null
                    )
            );
        }
    }
    
    /**
     * Build OTP response with masked OTP and expiry info
     * @param otpEntity OTP entity
     * @return OTP response
     */
    private OtpResponseDto buildOtpResponse(OtpEntity otpEntity) {
        // Mask the OTP for security (e.g., "1**4*6")
        String otp = otpEntity.getOtp();
        String maskedOtp = maskOtp(otp);
        
        // Calculate expiry time in minutes
        long expiryMinutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), otpEntity.getExpiresAt());
        
        return new OtpResponseDto(
                otpEntity.getRecipient(),
                "OTP sent successfully", 
                maskedOtp, 
                false,
                expiryMinutes
        );
    }
    
    /**
     * Mask the OTP for security
     * @param otp original OTP
     * @return masked OTP
     */
    private String maskOtp(String otp) {
        if (otp == null || otp.length() <= 2) {
            return "******";
        }
        
        StringBuilder masked = new StringBuilder(otp);
        for (int i = 1; i < otp.length() - 1; i++) {
            if (Math.random() < 0.7) { // 70% chance to mask each digit
                masked.setCharAt(i, '*');
            }
        }
        
        return masked.toString();
    }
} 