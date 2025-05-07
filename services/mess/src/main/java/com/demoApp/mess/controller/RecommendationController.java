package com.demoApp.mess.controller;

import com.demoApp.mess.dto.RecommendationResponse;
import com.demoApp.mess.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*", allowedHeaders = "*") // Allow cross-origin requests
public class RecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/messes")
    public ResponseEntity<List<RecommendationResponse>> getMessRecommendations(
            @RequestParam(defaultValue = "All") String category,
            @RequestParam(defaultValue = "All") String cuisine) {
        
        try {
            logger.info("Received recommendation request with category: {}, cuisine: {}", category, cuisine);
            List<RecommendationResponse> recommendations = recommendationService.getMessRecommendations(category, cuisine);
            
            if (recommendations.isEmpty()) {
                logger.info("No recommendations found for category: {}, cuisine: {}", category, cuisine);
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            logger.error("Error getting recommendations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
    
    /**
     * Test endpoint to check serialization of response objects.
     * This helps the frontend team by providing sample data in the expected format.
     */
    @GetMapping("/test")
    public ResponseEntity<List<RecommendationResponse>> getTestRecommendations() {
        try {
            logger.info("Received test recommendation request");
            
            RecommendationResponse recommendation1 = RecommendationResponse.builder()
                .messId(1L)
                .messName("Hotel Malabar")
                .category("Dinner")
                .cuisine("North Indian")
                .score(4.8)
                .address("123 Main Street, Bangalore")
                .location("Bangalore")
                .recommendedItems(new String[]{"Butter Chicken", "Naan", "Paneer Tikka"})
                .build();
                
            RecommendationResponse recommendation2 = RecommendationResponse.builder()
                .messId(2L)
                .messName("Spice Garden")
                .category("Breakfast")
                .cuisine("South Indian")
                .score(4.9)
                .address("456 Park Avenue, Bangalore")
                .location("Bangalore")
                .recommendedItems(new String[]{"Masala Dosa", "Idli", "Vada"})
                .build();
                
            RecommendationResponse recommendation3 = RecommendationResponse.builder()
                .messId(3L)
                .messName("Food Paradise")
                .category("Lunch")
                .cuisine("Continental")
                .score(4.4)
                .address("789 Beach Road, Mumbai")
                .location("Mumbai")
                .recommendedItems(new String[]{"Pasta", "Pizza", "Burger"})
                .build();
                
            return ResponseEntity.ok(Arrays.asList(recommendation1, recommendation2, recommendation3));
        } catch (Exception e) {
            logger.error("Error getting test recommendations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
} 