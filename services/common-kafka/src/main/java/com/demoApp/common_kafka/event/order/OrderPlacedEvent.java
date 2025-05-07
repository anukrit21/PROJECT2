package com.demoApp.common_kafka.event.order;

import com.demoApp.common_kafka.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderPlacedEvent extends BaseEvent {

    private UUID orderId;
    private UUID userId;
    private UUID messId;
    private LocalDateTime orderTime;
    private String status; // PENDING, CONFIRMED, PREPARING, READY, DELIVERED, CANCELED
    private BigDecimal totalAmount;
    private String deliveryOption; // PICKUP, DELIVERY
    private String paymentStatus; // PENDING, PAID, FAILED
    private UUID deliveryAddressId;
    private List<OrderItem> items;    
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItem {
        private UUID menuItemId;
        private String name;
        private int quantity;
        private BigDecimal unitPrice;
        private String specialInstructions;
    }
}
