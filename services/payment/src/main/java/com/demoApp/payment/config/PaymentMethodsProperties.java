package com.demoApp.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration for enabling/disabling payment methods
 * All methods are processed through Stripe
 */
@Component
@ConfigurationProperties(prefix = "payment.methods")
@Data
public class PaymentMethodsProperties {
    
    /**
     * Credit card payment method
     */
    private PaymentMethodConfig creditCard = new PaymentMethodConfig(true);
    
    /**
     * Debit card payment method
     */
    private PaymentMethodConfig debitCard = new PaymentMethodConfig(true);
    
    /**
     * UPI payment method
     */
    private PaymentMethodConfig upi = new PaymentMethodConfig(true);
    
    /**
     * Net banking payment method
     */
    private PaymentMethodConfig netbanking = new PaymentMethodConfig(true);
    
    /**
     * Digital wallet payment method
     */
    private PaymentMethodConfig wallet = new PaymentMethodConfig(true);
    
    /**
     * Inner class for payment method configuration
     */
    @Data
    public static class PaymentMethodConfig {
        private boolean enabled;
        
        public PaymentMethodConfig(boolean enabled) {
            this.enabled = enabled;
        }
        
        public PaymentMethodConfig() {
            this(false);
        }
    }
} 