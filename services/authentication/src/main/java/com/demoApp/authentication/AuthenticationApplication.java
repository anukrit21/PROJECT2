package com.demoApp.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Authentication Service Application
 * 
 * Note: Currently using temporary implementations for TOTP MFA functionality
 * to workaround dependency issues. See SecurityConfigTemp and AuthenticationServiceImplTemp.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthenticationApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthenticationApplication.class, args);
    }
} 