package com.demoApp.mess.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "mess.recommendation")
@Data
public class RecommendationConfig {
    
    private boolean enabled = true;
    private Map<String, String> mlFiles;
    private String defaultCategory = "All";
    private String defaultCuisine = "All";
    private double minScoreThreshold = 3.0;
    
    // Convenience getters for specific files
    public String getMessMasterFile() {
        return mlFiles != null ? mlFiles.getOrDefault("mess-master", "ml/mess_master.csv") : "ml/mess_master.csv";
    }
    
    public String getMenuMessFile() {
        return mlFiles != null ? mlFiles.getOrDefault("menu-mess", "ml/menu_mess.csv") : "ml/menu_mess.csv";
    }
    
    public String getRatingsFile() {
        return mlFiles != null ? mlFiles.getOrDefault("ratings", "ml/updated_dataset.csv") : "ml/updated_dataset.csv";
    }
} 