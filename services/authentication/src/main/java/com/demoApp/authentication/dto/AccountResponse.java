package com.demoApp.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private String status;
    private String message;
    private boolean success;
    private boolean accountLocked;
    private String username;
} 