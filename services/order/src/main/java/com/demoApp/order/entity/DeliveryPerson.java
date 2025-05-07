package com.demoApp.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "delivery_persons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPerson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contactNumber;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryPersonStatus status;

    @Column(nullable = false)
    private LocalTime shiftStartTime;

    @Column(nullable = false)
    private LocalTime shiftEndTime;

    @Column(nullable = false)
    private Integer maxOrderCapacity;

    @Column(nullable = false)
    private Integer currentOrderCount;

    @OneToMany(mappedBy = "deliveryPerson")
    private List<Order> activeOrders;

    @Column(nullable = false)
    private Boolean isAvailable;

    @Column(nullable = false)
    private String currentLocation;

    @Column(nullable = false)
    private Double rating;

    @Column(nullable = false)
    private Integer totalDeliveries;

    @Column(nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private String vehicleType;

    public boolean canAcceptOrder() {
        return isAvailable && 
               status == DeliveryPersonStatus.AVAILABLE && 
               currentOrderCount < maxOrderCapacity;
    }

    public void incrementOrderCount() {
        this.currentOrderCount++;
        if (this.currentOrderCount >= this.maxOrderCapacity) {
            this.status = DeliveryPersonStatus.ON_DELIVERY;
            this.isAvailable = false;
        }
    }

    public void decrementOrderCount() {
        if (this.currentOrderCount > 0) {
            this.currentOrderCount--;
            if (this.currentOrderCount < this.maxOrderCapacity) {
                this.status = DeliveryPersonStatus.AVAILABLE;
                this.isAvailable = true;
            }
        }
    }

    public void updateRating(Double newRating) {
        if (this.rating == null) {
            this.rating = newRating;
        } else {
            this.rating = (this.rating * this.totalDeliveries + newRating) / (this.totalDeliveries + 1);
        }
        this.totalDeliveries++;
    }
} 