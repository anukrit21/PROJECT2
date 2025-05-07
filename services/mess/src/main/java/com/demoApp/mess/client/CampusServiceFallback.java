package com.demoApp.mess.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CampusServiceFallback implements CampusServiceClient {
    private static final Logger logger = LoggerFactory.getLogger(CampusServiceFallback.class);
    
    @Override
    public Map<String, Object> getCampusById(Long campusId) {
        logger.warn("Fallback: Unable to get campus details for ID: {}", campusId);
        return getFallbackCampus(campusId);
    }
    
    @Override
    public List<Map<String, Object>> getAllCampuses() {
        logger.warn("Fallback: Unable to get all campuses");
        List<Map<String, Object>> fallbackCampuses = new ArrayList<>();
        fallbackCampuses.add(getFallbackCampus(1L));
        fallbackCampuses.add(getFallbackCampus(2L));
        return fallbackCampuses;
    }
    
    @Override
    public List<Map<String, Object>> getCampusesByLocation(String location) {
        logger.warn("Fallback: Unable to get campuses for location: {}", location);
        List<Map<String, Object>> fallbackCampuses = new ArrayList<>();
        fallbackCampuses.add(getFallbackCampus(1L));
        return fallbackCampuses;
    }
    
    @Override
    public List<Map<String, Object>> getMessesByCampusId(Long campusId) {
        logger.warn("Fallback: Unable to get messes for campus ID: {}", campusId);
        return getFallbackMesses();
    }
    
    private Map<String, Object> getFallbackCampus(Long campusId) {
        Map<String, Object> campus = new HashMap<>();
        campus.put("campusId", campusId);
        
        if (campusId == 1L) {
            campus.put("name", "Main Campus");
            campus.put("location", "Bangalore");
            campus.put("address", "123 University Road, Bangalore 560001");
        } else {
            campus.put("name", "North Campus");
            campus.put("location", "Delhi");
            campus.put("address", "456 College Street, Delhi 110001");
        }
        
        campus.put("contactNumber", "+91-9876543210");
        campus.put("email", "info@demoapp.edu");
        campus.put("active", true);
        
        return campus;
    }
    
    private List<Map<String, Object>> getFallbackMesses() {
        List<Map<String, Object>> messes = new ArrayList<>();
        
        Map<String, Object> mess1 = new HashMap<>();
        mess1.put("messId", 1L);
        mess1.put("messName", "Royal Dining");
        mess1.put("location", "Bangalore");
        mess1.put("address", "123 Food Street, Bangalore 560001");
        mess1.put("contactNumber", "+91-9876543211");
        mess1.put("category", "All");
        mess1.put("cuisine", "North Indian");
        mess1.put("rating", 4.5);
        messes.add(mess1);
        
        Map<String, Object> mess2 = new HashMap<>();
        mess2.put("messId", 2L);
        mess2.put("messName", "South Spice");
        mess2.put("location", "Bangalore");
        mess2.put("address", "456 Taste Avenue, Bangalore 560001");
        mess2.put("contactNumber", "+91-9876543212");
        mess2.put("category", "All");
        mess2.put("cuisine", "South Indian");
        mess2.put("rating", 4.3);
        messes.add(mess2);
        
        return messes;
    }
} 