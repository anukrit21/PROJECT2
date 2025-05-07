package com.demoApp.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String messId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_person_id")
    private DeliveryPerson deliveryPerson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDateTime orderTime;

    private LocalDateTime deliveryTime;

    @Column(nullable = false)
    private String deliveryLocation;

    private String specialInstructions;

    @Column(nullable = false)
    private Boolean isSubscriptionOrder;

    @Column(nullable = false)
    private String paymentStatus;

    @Column(nullable = false)
    private String paymentId;

    @Column(nullable = false)
    private LocalDateTime expectedDeliveryTime;
} 