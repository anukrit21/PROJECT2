package com.demoApp.common_kafka.event.order;

import com.demoApp.common_kafka.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderStatusChangedEvent extends BaseEvent {
    
    private UUID orderId;
    private UUID userId;
    private UUID messId;
    private String previousStatus;
    private String newStatus; // PENDING, CONFIRMED, PREPARING, READY, DELIVERED, CANCELED
    private LocalDateTime statusChangeTime;
    private String statusChangeReason;
    private String changedBy; // USER, SYSTEM, ADMIN, MESS_OWNER
    
    
}
