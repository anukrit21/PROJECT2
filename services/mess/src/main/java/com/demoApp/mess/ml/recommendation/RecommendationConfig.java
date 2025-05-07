package com.demoApp.mess.ml.recommendation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
public class RecommendationConfig {

    @Autowired
    private com.demoApp.mess.config.RecommendationConfig recommendationConfig;

    @Bean
    public MessRecommendationModel messRecommendationModel() {
        return new MessRecommendationModel(recommendationConfig);
    }
} 