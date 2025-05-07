package com.demoApp.payment.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Stripe API
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class StripeConfig {

    private final StripeProperties stripeProperties;
    
    /**
     * Get the Stripe publishable key
     */
    public String getPublishableKey() {
        return stripeProperties.getPublicKey();
    }
    
    /**
     * Get the Stripe API key
     */
    public String getApiKey() {
        return stripeProperties.getApiKey();
    }
    
    /**
     * Get the Stripe webhook secret
     */
    public String getWebhookSecret() {
        return stripeProperties.getWebhookSecret();
    }

    /**
     * Initialize Stripe API with the API key
     */
    @PostConstruct
    public void init() {
        try {
            log.info("Initializing Stripe API with API key");
            Stripe.apiKey = stripeProperties.getApiKey();
        } catch (Exception e) {
            log.error("Error initializing Stripe API", e);
        }
    }
} 