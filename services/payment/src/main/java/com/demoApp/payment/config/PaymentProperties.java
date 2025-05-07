package com.demoApp.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for payment options
 */
@Component
@ConfigurationProperties(prefix = "payment")
@Data
public class PaymentProperties {

    /**
     * Payment gateway API key
     */
    private String apiKey = "test_key";

    /**
     * Payment gateway secret
     */
    private String secret = "test_secret";
}
