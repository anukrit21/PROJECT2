package com.demoApp.owner.client.fallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.demoApp.owner.client.MessServiceClient;

import java.util.Collections;

@Component
public class MessServiceFallback implements MessServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MessServiceFallback.class);
    
    @Override
    public ResponseEntity<Object> getMessesByOwnerId(Long ownerId) {
        log.warn("Falling back to empty response for mess service, owner ID: {}", ownerId);
        return ResponseEntity.ok(Collections.emptyList());
    }

    @Override
    public ResponseEntity<Object> getMessDetails(Long messId) {
        log.error("Fallback: Unable to connect to mess service for mess ID: {}", messId);
        return ResponseEntity.ok().build();
    }
    
    @Override
    public ResponseEntity<Object> getMessRecommendations(String category, String cuisine) {
        log.error("Fallback: Unable to connect to mess service for recommendations. Category: {}, Cuisine: {}", 
            category, cuisine);
        return ResponseEntity.ok(Collections.emptyList());
    }
}