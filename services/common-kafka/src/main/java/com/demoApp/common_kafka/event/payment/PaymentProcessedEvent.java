package com.demoApp.common_kafka.event.payment;

import com.demoApp.common_kafka.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentProcessedEvent extends BaseEvent {
    
    private UUID paymentId;
    private UUID userId;
    private UUID subscriptionId;
    private String paymentMethod;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String transactionId;
    
}
