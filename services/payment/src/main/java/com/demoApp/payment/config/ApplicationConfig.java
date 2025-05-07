package com.demoApp.payment.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
public class ApplicationConfig {

    /**
     * Configure property placeholder resolution
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        // Prevent failure when properties are missing
        configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }
    
    /**
     * ModelMapper bean configuration
     * Note: This replaces the bean defined in ModelMapperConfig which is now deprecated
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true);
        return modelMapper;
    }
} 