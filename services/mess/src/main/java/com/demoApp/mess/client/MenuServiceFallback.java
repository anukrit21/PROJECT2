package com.demoApp.mess.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MenuServiceFallback implements MenuServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(MenuServiceFallback.class);
    
    @Override
    public List<Map<String, Object>> getMenuItemsByMessId(Long messId) {
        logger.warn("Fallback: Unable to get menu items for mess ID: {}", messId);
        return getFallbackMenuItems();
    }
    
    @Override
    public List<Map<String, Object>> getMenuItemsByCategory(String category) {
        logger.warn("Fallback: Unable to get menu items for category: {}", category);
        return getFallbackMenuItems();
    }
    
    @Override
    public List<Map<String, Object>> getMenuItemsByCuisine(String cuisine) {
        logger.warn("Fallback: Unable to get menu items for cuisine: {}", cuisine);
        return getFallbackMenuItems();
    }
    
    @Override
    public List<Map<String, Object>> getPopularMenuItems(int limit) {
        logger.warn("Fallback: Unable to get popular menu items");
        return getFallbackMenuItems();
    }
    
    private List<Map<String, Object>> getFallbackMenuItems() {
        List<Map<String, Object>> fallbackItems = new ArrayList<>();
        
        Map<String, Object> item1 = new HashMap<>();
        item1.put("itemId", 1L);
        item1.put("name", "Butter Chicken");
        item1.put("description", "Creamy tomato-based curry with tender chicken pieces");
        item1.put("price", 250.0);
        item1.put("category", "Main Course");
        item1.put("cuisine", "North Indian");
        item1.put("messId", 1L);
        item1.put("isVegetarian", false);
        item1.put("isAvailable", true);
        fallbackItems.add(item1);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("itemId", 2L);
        item2.put("name", "Masala Dosa");
        item2.put("description", "Crispy rice crepe filled with spiced potato filling");
        item2.put("price", 120.0);
        item2.put("category", "Breakfast");
        item2.put("cuisine", "South Indian");
        item2.put("messId", 2L);
        item2.put("isVegetarian", true);
        item2.put("isAvailable", true);
        fallbackItems.add(item2);
        
        Map<String, Object> item3 = new HashMap<>();
        item3.put("itemId", 3L);
        item3.put("name", "Paneer Tikka");
        item3.put("description", "Marinated and grilled cottage cheese with spices");
        item3.put("price", 180.0);
        item3.put("category", "Appetizer");
        item3.put("cuisine", "North Indian");
        item3.put("messId", 1L);
        item3.put("isVegetarian", true);
        item3.put("isAvailable", true);
        fallbackItems.add(item3);
        
        return fallbackItems;
    }
} 