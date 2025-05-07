package com.demoApp.payment.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for payment gateway
 */
@Configuration
@Getter
@RequiredArgsConstructor
public class PaymentGatewayConfig {
    
    private final PaymentProperties paymentProperties;
    
    /**
     * Get payment gateway API key
     */
    public String getApiKey() {
        return paymentProperties.getApiKey();
    }
    
    /**
     * Get payment gateway secret
     */
    public String getSecret() {
        return paymentProperties.getSecret();
    }
} 