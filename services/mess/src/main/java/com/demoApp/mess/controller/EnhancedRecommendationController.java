package com.demoApp.mess.controller;

import com.demoApp.mess.client.CampusServiceClient;
import com.demoApp.mess.client.MenuServiceClient;
import com.demoApp.mess.dto.RecommendationResponse;
import com.demoApp.mess.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enhanced-recommendations")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class EnhancedRecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedRecommendationController.class);
    
    private final RecommendationService recommendationService;
    private final MenuServiceClient menuServiceClient;
    private final CampusServiceClient campusServiceClient;

    
    public EnhancedRecommendationController(
            RecommendationService recommendationService,
            MenuServiceClient menuServiceClient,
            CampusServiceClient campusServiceClient) {
        this.recommendationService = recommendationService;
        this.menuServiceClient = menuServiceClient;
        this.campusServiceClient = campusServiceClient;
    }

    @GetMapping("/messes")
    public ResponseEntity<List<Map<String, Object>>> getEnhancedMessRecommendations(
            @RequestParam(defaultValue = "All") String category,
            @RequestParam(defaultValue = "All") String cuisine,
            @RequestParam(defaultValue = "All") String location) {
        
        try {
            logger.info("Received enhanced recommendation request - category: {}, cuisine: {}, location: {}", 
                    category, cuisine, location);
            
            // Get basic recommendations
            List<RecommendationResponse> baseRecommendations = 
                    recommendationService.getMessRecommendations(category, cuisine);
            
            if (baseRecommendations.isEmpty()) {
                logger.info("No recommendations found for category: {}, cuisine: {}", category, cuisine);
                return ResponseEntity.ok(Collections.emptyList());
            }
            
            // Enhance recommendations with menu and campus data
            List<Map<String, Object>> enhancedRecommendations = new ArrayList<>();
            for (RecommendationResponse rec : baseRecommendations) {
                Map<String, Object> enhancedRec = new HashMap<>();
                
                // Copy base recommendation data
                enhancedRec.put("messId", rec.getMessId());
                enhancedRec.put("messName", rec.getMessName());
                enhancedRec.put("category", rec.getCategory());
                enhancedRec.put("cuisine", rec.getCuisine());
                enhancedRec.put("score", rec.getScore());
                enhancedRec.put("address", rec.getAddress());
                enhancedRec.put("location", rec.getLocation());
                enhancedRec.put("recommendedItems", rec.getRecommendedItems());
                
                try {
                    // Get menu items for this mess
                    List<Map<String, Object>> menuItems = 
                            menuServiceClient.getMenuItemsByMessId(rec.getMessId());
                    enhancedRec.put("menuItems", menuItems);
                    
                    // Get campus information if location matches
                    if ("All".equalsIgnoreCase(location) || 
                            (rec.getLocation() != null && rec.getLocation().equalsIgnoreCase(location))) {
                        List<Map<String, Object>> campuses = 
                                campusServiceClient.getCampusesByLocation(rec.getLocation());
                        enhancedRec.put("campusInfo", campuses);
                    }
                } catch (Exception e) {
                    logger.warn("Error retrieving additional data for mess ID: {}", rec.getMessId(), e);
                }
                
                enhancedRecommendations.add(enhancedRec);
            }
            
            // Filter by location if specified and not "All"
            if (!"All".equalsIgnoreCase(location)) {
                enhancedRecommendations = enhancedRecommendations.stream()
                        .filter(rec -> {
                            String recLocation = (String) rec.get("location");
                            return recLocation != null && recLocation.equalsIgnoreCase(location);
                        })
                        .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(enhancedRecommendations);
        } catch (Exception e) {
            logger.error("Error getting enhanced recommendations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<List<Map<String, Object>>> getTestEnhancedRecommendations() {
        try {
            logger.info("Received test enhanced recommendation request");
            
            // Create sample enhanced recommendations
            List<Map<String, Object>> testRecommendations = new ArrayList<>();
            
            // Recommendation 1
            Map<String, Object> rec1 = new HashMap<>();
            rec1.put("messId", 1L);
            rec1.put("messName", "Royal Dining");
            rec1.put("category", "Dinner");
            rec1.put("cuisine", "North Indian");
            rec1.put("score", 4.8);
            rec1.put("address", "123 Food Street, Bangalore 560001");
            rec1.put("location", "Bangalore");
            rec1.put("recommendedItems", new String[]{"Butter Chicken", "Naan", "Paneer Tikka"});
            
            // Add menu items
            List<Map<String, Object>> menuItems1 = new ArrayList<>();
            Map<String, Object> item1 = new HashMap<>();
            item1.put("itemId", 101L);
            item1.put("name", "Butter Chicken");
            item1.put("price", 250.0);
            item1.put("isVegetarian", false);
            menuItems1.add(item1);
            
            Map<String, Object> item2 = new HashMap<>();
            item2.put("itemId", 102L);
            item2.put("name", "Naan");
            item2.put("price", 40.0);
            item2.put("isVegetarian", true);
            menuItems1.add(item2);
            
            rec1.put("menuItems", menuItems1);
            
            // Add campus info
            List<Map<String, Object>> campusInfo1 = new ArrayList<>();
            Map<String, Object> campus1 = new HashMap<>();
            campus1.put("campusId", 1L);
            campus1.put("name", "Main Campus");
            campus1.put("location", "Bangalore");
            campusInfo1.add(campus1);
            rec1.put("campusInfo", campusInfo1);
            
            testRecommendations.add(rec1);
            
            // Recommendation 2
            Map<String, Object> rec2 = new HashMap<>();
            rec2.put("messId", 2L);
            rec2.put("messName", "South Spice");
            rec2.put("category", "Breakfast");
            rec2.put("cuisine", "South Indian");
            rec2.put("score", 4.9);
            rec2.put("address", "456 Taste Avenue, Bangalore 560001");
            rec2.put("location", "Bangalore");
            rec2.put("recommendedItems", new String[]{"Masala Dosa", "Idli", "Vada"});
            
            // Add menu items
            List<Map<String, Object>> menuItems2 = new ArrayList<>();
            Map<String, Object> item3 = new HashMap<>();
            item3.put("itemId", 201L);
            item3.put("name", "Masala Dosa");
            item3.put("price", 120.0);
            item3.put("isVegetarian", true);
            menuItems2.add(item3);
            
            Map<String, Object> item4 = new HashMap<>();
            item4.put("itemId", 202L);
            item4.put("name", "Idli");
            item4.put("price", 80.0);
            item4.put("isVegetarian", true);
            menuItems2.add(item4);
            
            rec2.put("menuItems", menuItems2);
            
            // Add campus info (same campus as recommendation 1)
            rec2.put("campusInfo", campusInfo1);
            
            testRecommendations.add(rec2);
            
            return ResponseEntity.ok(testRecommendations);
        } catch (Exception e) {
            logger.error("Error getting test enhanced recommendations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
    
    @GetMapping("/menu-items/popular")
    public ResponseEntity<List<Map<String, Object>>> getPopularMenuItems() {
        try {
            logger.info("Fetching popular menu items");
            List<Map<String, Object>> popularItems = menuServiceClient.getPopularMenuItems(5);
            return ResponseEntity.ok(popularItems);
        } catch (Exception e) {
            logger.error("Error fetching popular menu items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
    
    @GetMapping("/campus/{campusId}/messes")
    public ResponseEntity<List<Map<String, Object>>> getMessesByCampus(@PathVariable Long campusId) {
        try {
            logger.info("Fetching messes for campus: {}", campusId);
            List<Map<String, Object>> messes = campusServiceClient.getMessesByCampusId(campusId);
            return ResponseEntity.ok(messes);
        } catch (Exception e) {
            logger.error("Error fetching messes for campus: {}", campusId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
} 