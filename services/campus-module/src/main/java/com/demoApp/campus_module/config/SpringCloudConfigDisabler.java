package com.demoApp.campus_module.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SpringCloudConfigDisabler {

    @Bean
    ApplicationListener<ApplicationEnvironmentPreparedEvent> environmentPreparedEventListener() {
        return event -> {
            ConfigurableEnvironment environment = event.getEnvironment();
            Map<String, Object> map = new HashMap<>();
            map.put("spring.cloud.config.enabled", false);
            map.put("spring.cloud.config.retry.max-interval", 5000);
            map.put("spring.cloud.config.retry.initial-interval", 1000);
            map.put("spring.cloud.compatibility-verifier.enabled", false);
            map.put("eureka.client.enabled", false);
            map.put("spring.cloud.discovery.enabled", false);
            
            MapPropertySource propertySource = new MapPropertySource("programmaticallyDisableCloudConfig", map);
            environment.getPropertySources().addFirst(propertySource);
            
            System.out.println("Spring Cloud Config has been disabled programmatically");
        };
    }
} 