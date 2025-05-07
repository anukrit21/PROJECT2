package com.demoApp.delivery.controller;

import com.demoApp.delivery.dto.DeliveryDTO;
import com.demoApp.delivery.service.DeliveryTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/delivery-tracking")
@RequiredArgsConstructor
public class DeliveryTrackingController {

    private final DeliveryTrackingService deliveryTrackingService;

    /**
     * Update current location of a delivery (called by delivery person's app)
     */
    @PutMapping("/{deliveryId}/location")
    public ResponseEntity<?> updateDeliveryLocation(
            @PathVariable Long deliveryId,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        
        DeliveryDTO updatedDelivery = deliveryTrackingService.updateDeliveryLocation(
                deliveryId, latitude, longitude);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Location updated successfully",
                "data", updatedDelivery
        ));
    }
    
    /**
     * Get current tracking data for a delivery (called by customer's app)
     */
    @GetMapping("/{deliveryId}")
    public ResponseEntity<?> getTrackingData(@PathVariable Long deliveryId) {
        Map<String, Object> trackingData = deliveryTrackingService.getTrackingData(deliveryId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", trackingData
        ));
    }
    
    /**
     * Get estimated time of arrival for a delivery (called by customer's app)
     */
    @GetMapping("/{deliveryId}/eta")
    public ResponseEntity<?> getEstimatedTimeOfArrival(@PathVariable Long deliveryId) {
        Map<String, Object> eta = deliveryTrackingService.getEstimatedTimeOfArrival(deliveryId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", eta
        ));
    }
} 