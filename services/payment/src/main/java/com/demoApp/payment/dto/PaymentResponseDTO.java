package com.demoApp.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private String paymentIntentId;
    private String clientSecret;
    private String status;
    private BigDecimal amount;
    private String currency;

    private boolean requiresAction;
    private Long userId;
    private String setupIntentId;
    
    private Long ownerId;
    private Long merchantId;
}
