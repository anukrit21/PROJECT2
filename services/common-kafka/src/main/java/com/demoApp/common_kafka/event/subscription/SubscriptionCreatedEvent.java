package com.demoApp.common_kafka.event.subscription;

import com.demoApp.common_kafka.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SubscriptionCreatedEvent extends BaseEvent {
    
    private UUID subscriptionId;
    private UUID userId;
    private UUID messId;
    private String plan;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;
    private boolean active;
    
    /**
     * Constructor with initialization
     */
    public SubscriptionCreatedEvent(UUID subscriptionId, UUID userId, UUID messId, 
                                   String plan, LocalDate startDate, LocalDate endDate, 
                                   BigDecimal price, boolean active) {
        super();
        init("SUBSCRIPTION_CREATED", "subscription-service");
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.messId = messId;
        this.plan = plan;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.active = active;
    }
}
