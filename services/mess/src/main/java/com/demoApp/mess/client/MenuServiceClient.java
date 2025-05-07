package com.demoApp.mess.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

@FeignClient(name = "menu-service", fallback = MenuServiceFallback.class)
public interface MenuServiceClient {

    @GetMapping("/api/menu/items/mess/{messId}")
    List<Map<String, Object>> getMenuItemsByMessId(@PathVariable("messId") Long messId);
    
    @GetMapping("/api/menu/items/category")
    List<Map<String, Object>> getMenuItemsByCategory(@RequestParam("category") String category);
    
    @GetMapping("/api/menu/items/cuisine")
    List<Map<String, Object>> getMenuItemsByCuisine(@RequestParam("cuisine") String cuisine);
    
    @GetMapping("/api/menu/items/popular")
    List<Map<String, Object>> getPopularMenuItems(@RequestParam(value = "limit", defaultValue = "10") int limit);
} 