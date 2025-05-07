package com.demoApp.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Stripe
 */
@Component
@ConfigurationProperties(prefix = "stripe")
@Data
public class StripeProperties {

    /**
     * Stripe API key
     */
    private String apiKey = "sk_test_...";
    
    /**
     * Stripe webhook secret
     */
    private String webhookSecret = "whsec_...";

    /**
     * Stripe public key
     */
    private String publicKey = "pk_test_...";

    /**
     * Stripe secret key
     */
    private String secretKey = "sk_test_...";
}
