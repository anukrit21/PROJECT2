package com.demoApp.payment.dto;

import com.demoApp.payment.model.PaymentMethod;
import com.demoApp.payment.model.PaymentMethodType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for payment request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private BigDecimal tax;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    /**
     * Specific payment method type (e.g., VISA, MASTERCARD, GOOGLE_PAY)
     */
    private PaymentMethodType paymentMethodType;

    private String paymentMethodId;

    private String orderReference;

    private Long productId;

    private Long subscriptionId;

    private Long merchantId;

    private Long ownerId;

    // Change here: metadata should be a Map<String, String> instead of a String
    private Map<String, String> metadata;

    private String customerName;

    private String customerEmail;

    private String billingAddress;

    private String shippingAddress;

    // UPI-specific fields
    private String upiId;
    private String upiProvider; // PhonePe, Google Pay, Paytm, etc.

    // Netbanking-specific fields
    private String bankCode;
    private String accountNumber;

    // Display preferences
    private boolean showTaxBreakdown = true;
    private boolean showItemDetails = true;

    private boolean savePaymentMethod = false;

    // For creating a setup intent instead of a payment
    private boolean setupIntent = false;

    // For initiating recurring payments
    private boolean recurring = false;

    // Added field for customerId
    private String customerId;

    public String getCustomerId() {
        return customerId;
    }
}
