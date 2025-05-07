package com.demoApp.delivery.service;

import com.demoApp.delivery.dto.DeliveryDTO;
import com.demoApp.delivery.entity.Delivery;
import com.demoApp.delivery.repository.DeliveryRepository;
//import com.google.maps.DistanceMatrixApi;
//import com.google.maps.GeoApiContext;
//import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.LatLng;
//import com.google.maps.model.TravelMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryTrackingService {

    private final DeliveryRepository deliveryRepository;
    //private final GeoApiContext geoApiContext;
    private final ModelMapper modelMapper;
    
    /**
     * Update the current location of a delivery
     */
    @Transactional
    public DeliveryDTO updateDeliveryLocation(Long deliveryId, double latitude, double longitude) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with ID: " + deliveryId));
        
        delivery.setCurrentLatitude(latitude);
        delivery.setCurrentLongitude(longitude);
        delivery.setLocationUpdatedAt(LocalDateTime.now());
        
        Delivery updatedDelivery = deliveryRepository.save(delivery);
        log.info("Updated location for delivery {}: lat={}, lng={}", deliveryId, latitude, longitude);
        
        return modelMapper.map(updatedDelivery, DeliveryDTO.class);
    }
    
    /**
     * Get the estimated time of arrival for a delivery
     */
    public Map<String, Object> getEstimatedTimeOfArrival(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with ID: " + deliveryId));
        
        Map<String, Object> result = new HashMap<>();
        result.put("deliveryId", deliveryId);
        
        // If delivery is already delivered, return actual delivery time
        if (delivery.getStatus() == Delivery.DeliveryStatus.DELIVERED) {
            result.put("distance", 0);
            result.put("duration", 0);
            result.put("estimatedArrival", delivery.getDeliveredTime());
            return result;
        }
        
        // If no current location, we can't calculate ETA
        if (delivery.getLocationUpdatedAt() == null) {
            // Calculate estimated time based on distance and average speed if we have destination coordinates
            if (delivery.getDeliveryLatitude() != 0 && delivery.getDeliveryLongitude() != 0) {
                // Get coordinates from pickup point if available
                double startLat, startLng;
                if (delivery.getPickupPoint() != null) {
                    startLat = delivery.getPickupPoint().getLatitude();
                    startLng = delivery.getPickupPoint().getLongitude();
                } else {
                    // Fallback to current location (might be 0,0)
                    startLat = delivery.getCurrentLatitude();
                    startLng = delivery.getCurrentLongitude();
                }
                
                // Calculate rough estimate (will be replaced by actual API call when data is available)
                double distanceInMeters = calculateHaversineDistance(
                    startLat, startLng, 
                    delivery.getDeliveryLatitude(), delivery.getDeliveryLongitude()
                );
                
                // Assume average speed of 30 km/h in city traffic
                double speedInMetersPerSecond = 30 * 1000 / 3600; // 30 km/h converted to m/s
                long durationInSeconds = (long)(distanceInMeters / speedInMetersPerSecond);
                
                // Calculate estimated arrival time
                LocalDateTime estimatedArrival = LocalDateTime.now().plusSeconds(durationInSeconds);
                
                result.put("distance", distanceInMeters);
                result.put("duration", durationInSeconds);
                result.put("estimatedArrival", estimatedArrival);
                result.put("isEstimate", true);
                
                if (delivery.getPickupPoint() != null) {
                    result.put("currentLocation", Map.of(
                        "latitude", startLat,
                        "longitude", startLng,
                        "updatedAt", delivery.getCreatedAt()
                    ));
                }
                
                return result;
            } else {
                throw new RuntimeException("No location data available for this delivery");
            }
        }
        
        try {
            LatLng origin = new LatLng(delivery.getCurrentLatitude(), delivery.getCurrentLongitude());
            LatLng destination = new LatLng(delivery.getDeliveryLatitude(), delivery.getDeliveryLongitude());
            
            // Google Maps Distance Matrix API temporarily disabled
            /*
            DistanceMatrix distanceMatrix = DistanceMatrixApi.newRequest(geoApiContext)
                    .origins(origin)
                    .destinations(destination)
                    .mode(TravelMode.DRIVING)
                    .await();
            
            // Extract distance and duration information
            long distanceInMeters = distanceMatrix.rows[0].elements[0].distance.inMeters;
            long durationInSeconds = distanceMatrix.rows[0].elements[0].duration.inSeconds;
            */
            
            // Use Haversine formula for distance calculation instead
            double distanceInMeters = calculateHaversineDistance(
                origin.lat, origin.lng,
                destination.lat, destination.lng
            );
            
            // Assume average speed of 30 km/h in city traffic
            double speedInMetersPerSecond = 30 * 1000 / 3600; // 30 km/h converted to m/s
            long durationInSeconds = (long)(distanceInMeters / speedInMetersPerSecond);
            
            // Calculate estimated arrival time
            LocalDateTime estimatedArrival = LocalDateTime.now().plusSeconds(durationInSeconds);
            
            result.put("distance", distanceInMeters);
            result.put("duration", durationInSeconds);
            result.put("estimatedArrival", estimatedArrival);
            result.put("isEstimate", true); // Mark as estimate since we're not using Google Maps
            result.put("currentLocation", Map.of(
                    "latitude", delivery.getCurrentLatitude(),
                    "longitude", delivery.getCurrentLongitude(),
                    "updatedAt", delivery.getLocationUpdatedAt()
            ));
            
            return result;
        } catch (Exception e) {
            log.error("Error calculating ETA for delivery {}: {}", deliveryId, e.getMessage());
            throw new RuntimeException("Failed to calculate ETA: " + e.getMessage());
        }
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     * Used for rough distance estimation when Google Maps API is not available
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Earth's radius in meters
        final double R = 6371000;
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
                   
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        
        return distance;
    }
    
    /**
     * Get the current tracking data for a delivery
     */
    public Map<String, Object> getTrackingData(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found with ID: " + deliveryId));
        
        Map<String, Object> trackingData = new HashMap<>();
        trackingData.put("deliveryId", deliveryId);
        trackingData.put("status", delivery.getStatus());
        
        // Current location
        if (delivery.getLocationUpdatedAt() != null) {
            trackingData.put("currentLocation", Map.of(
                    "latitude", delivery.getCurrentLatitude(),
                    "longitude", delivery.getCurrentLongitude(),
                    "updatedAt", delivery.getLocationUpdatedAt()
            ));
        } else {
            // If no real-time tracking data is available yet, use pickup point as the current location
            if (delivery.getPickupPoint() != null) {
                trackingData.put("currentLocation", Map.of(
                        "latitude", delivery.getPickupPoint().getLatitude(),
                        "longitude", delivery.getPickupPoint().getLongitude(),
                        "updatedAt", delivery.getCreatedAt()
                ));
            }
        }
        
        // Destination
        if (delivery.getDeliveryAddress() != null) {
            trackingData.put("destination", Map.of(
                    "address", delivery.getDeliveryAddress() != null ? delivery.getDeliveryAddress() : "Not specified",
                    "latitude", delivery.getDeliveryLatitude(),
                    "longitude", delivery.getDeliveryLongitude()
            ));
        }
        
        // Pickup point
        if (delivery.getPickupPoint() != null) {
            trackingData.put("pickupPoint", Map.of(
                    "id", delivery.getPickupPoint().getId(),
                    "name", delivery.getPickupPoint().getName(),
                    "address", delivery.getPickupPoint().getAddress(),
                    "latitude", delivery.getPickupPoint().getLatitude(),
                    "longitude", delivery.getPickupPoint().getLongitude(),
                    "zone", delivery.getPickupPoint().getCampusZone()
            ));
        }
        
        // Delivery person
        if (delivery.getDeliveryPerson() != null) {
            trackingData.put("deliveryPerson", Map.of(
                    "id", delivery.getDeliveryPerson().getId(),
                    "name", delivery.getDeliveryPerson().getName(),
                    "mobileNumber", delivery.getDeliveryPerson().getMobileNumber(),
                    "vehicleType", delivery.getDeliveryPerson().getVehicleType(),
                    "rating", delivery.getDeliveryPerson().getRating()
            ));
        }
        
        // Try to get ETA if we have location data
        if (delivery.getLocationUpdatedAt() != null) {
            try {
                Map<String, Object> eta = getEstimatedTimeOfArrival(deliveryId);
                trackingData.put("eta", eta.get("estimatedArrival"));
                trackingData.put("remainingDistance", eta.get("distance"));
                trackingData.put("remainingDuration", eta.get("duration"));
            } catch (Exception e) {
                log.warn("Could not calculate ETA for delivery {}: {}", deliveryId, e.getMessage());
            }
        }
        
        return trackingData;
    }
} 