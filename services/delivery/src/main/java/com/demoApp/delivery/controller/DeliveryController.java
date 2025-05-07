package com.demoApp.delivery.controller;

import com.demoApp.delivery.dto.DeliveryDTO;
import com.demoApp.delivery.dto.DeliveryRequestDTO;
import com.demoApp.delivery.entity.Delivery;
import com.demoApp.delivery.service.DeliveryService;
import com.demoApp.delivery.service.DeliveryTrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryTrackingService deliveryTrackingService;
    private final ModelMapper modelMapper;
    
    @PostMapping
    public ResponseEntity<DeliveryDTO> createDelivery(@Valid @RequestBody DeliveryRequestDTO deliveryRequestDTO) {
        DeliveryDTO deliveryDTO = modelMapper.map(deliveryRequestDTO, DeliveryDTO.class);
        return new ResponseEntity<>(deliveryService.createDelivery(deliveryDTO), HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryDTO> getDeliveryById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(id));
    }
    
    @GetMapping
    public ResponseEntity<List<DeliveryDTO>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeliveryDTO>> getDeliveriesByStatus(
            @PathVariable Delivery.DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.getDeliveriesByStatus(status));
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryDTO> updateDeliveryStatus(
            @PathVariable Long id, 
            @RequestParam Delivery.DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.updateDeliveryStatus(id, status));
    }
    
    @PutMapping("/{id}/assign")
    public ResponseEntity<DeliveryDTO> assignDeliveryPerson(
            @PathVariable Long id,
            @RequestParam Long deliveryPersonId) {
        return ResponseEntity.ok(deliveryService.assignDeliveryPerson(id, deliveryPersonId));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DeliveryDTO> updateDelivery(
            @PathVariable Long id,
            @RequestBody DeliveryDTO deliveryDTO) {
        return ResponseEntity.ok(deliveryService.updateDelivery(id, deliveryDTO));
    }
    
    // Tracking endpoints
    @GetMapping("/{id}/tracking")
    public ResponseEntity<?> getTrackingData(@PathVariable Long id) {
        try {
            Map<String, Object> trackingData = deliveryTrackingService.getTrackingData(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", trackingData
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/{id}/eta")
    public ResponseEntity<?> getEstimatedTimeOfArrival(@PathVariable Long id) {
        try {
            Map<String, Object> eta = deliveryTrackingService.getEstimatedTimeOfArrival(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", eta
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    @PutMapping("/{id}/location")
    public ResponseEntity<?> updateDeliveryLocation(
            @PathVariable Long id,
            @RequestParam double latitude,
            @RequestParam double longitude) {
        
        try {
            DeliveryDTO updatedDelivery = deliveryTrackingService.updateDeliveryLocation(id, latitude, longitude);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Location updated successfully",
                    "data", updatedDelivery
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
