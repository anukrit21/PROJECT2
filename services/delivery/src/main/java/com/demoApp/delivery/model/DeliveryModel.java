package com.demoApp.delivery.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity(name = "DeliveryModel")
@Table(name = "delivery_models")
public class DeliveryModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long orderId;
    
    @Column(nullable = false)
    private Long deliveryPartnerId;
    
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
    
    private String pickupAddress;
    private String deliveryAddress;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime estimatedDeliveryTime;
    private String trackingNumber;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum DeliveryStatus {
        PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED
    }
} 