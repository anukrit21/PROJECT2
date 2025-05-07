package com.demoApp.delivery.config;

import com.google.maps.GeoApiContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Google Maps integration temporarily disabled - using mock implementation
 */
@Configuration
public class GoogleMapsConfig {
    
    // Commented out real implementation
    /*
    @Value("${google.maps.api.key}")
    private String apiKey;
    
    @Bean
    public GeoApiContext geoApiContext() {
        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }
    */
    
    /**
     * Mock implementation that doesn't require actual Google Maps API
     */
    @Bean
    GeoApiContext geoApiContext() {
        // This creates a GeoApiContext with a dummy API key
        // It won't make actual API calls but prevents null pointer exceptions
        return new GeoApiContext.Builder()
                .apiKey("mock-api-key")
                .build();
    }
} 