package com.demoApp.mess.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Map;

@FeignClient(name = "campus-service", fallback = CampusServiceFallback.class)
public interface CampusServiceClient {

    @GetMapping("/api/campus/{campusId}")
    Map<String, Object> getCampusById(@PathVariable("campusId") Long campusId);
    
    @GetMapping("/api/campus")
    List<Map<String, Object>> getAllCampuses();
    
    @GetMapping("/api/campus/location")
    List<Map<String, Object>> getCampusesByLocation(@RequestParam("location") String location);
    
    @GetMapping("/api/campus/{campusId}/messes")
    List<Map<String, Object>> getMessesByCampusId(@PathVariable("campusId") Long campusId);
} 