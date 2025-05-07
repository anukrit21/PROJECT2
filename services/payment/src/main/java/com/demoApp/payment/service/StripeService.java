package com.demoApp.payment.service;

import com.demoApp.payment.config.StripeProperties;
import com.demoApp.payment.dto.PaymentRequestDTO;
import com.demoApp.payment.entity.PaymentCustomer;
import com.demoApp.payment.exception.PaymentException;
import com.demoApp.payment.repository.PaymentCustomerRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentSource;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private final StripeProperties stripeProperties;
    private final PaymentCustomerRepository paymentCustomerRepository;
    private boolean stripeAvailable = true;

    @PostConstruct
    public void init() {
        log.info("Initializing Stripe with API key");
        if (stripeProperties.getApiKey() == null || stripeProperties.getApiKey().startsWith("sk_test_dummy")) {
            log.warn("Using dummy Stripe API key. Webhook validation will not work properly.");
            stripeAvailable = false;
            return;
        }
        try {
            Stripe.apiKey = stripeProperties.getApiKey();
            log.info("Stripe initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing Stripe API: {}", e.getMessage());
            stripeAvailable = false;
        }
    }

    // Create a payment intent with Stripe
    public PaymentIntent createPaymentIntent(PaymentRequestDTO paymentRequestDTO) {
        if (!stripeAvailable) {
            log.warn("Stripe API not available, returning simulated PaymentIntent");
            // Since we're returning null for mock PaymentIntent, we need to handle this in the caller
            return createMockPaymentIntent(
                paymentRequestDTO.getAmount().multiply(new BigDecimal("100")).longValue(),
                paymentRequestDTO.getCurrency(),
                paymentRequestDTO.getDescription()
            );
        }
        
        try {
            log.info("Creating payment intent for amount: {}, currency: {}, method: {}, type: {}", 
                    paymentRequestDTO.getAmount(), 
                    paymentRequestDTO.getCurrency(),
                    paymentRequestDTO.getPaymentMethod(),
                    paymentRequestDTO.getPaymentMethodType());

            Long amountInSmallestUnit = paymentRequestDTO.getAmount()
                    .multiply(new BigDecimal("100"))
                    .longValue();
            
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount(amountInSmallestUnit)
                .setCurrency(paymentRequestDTO.getCurrency())
                .setDescription(paymentRequestDTO.getDescription())
                .putAllMetadata(paymentRequestDTO.getMetadata());
            
            // First handle the specific payment method type if provided
            if (paymentRequestDTO.getPaymentMethodType() != null) {
                String stripeMethod = paymentRequestDTO.getPaymentMethodType().getStripeMethod();
                String subtype = paymentRequestDTO.getPaymentMethodType().getSubtype();
                
                paramsBuilder.addPaymentMethodType(stripeMethod);
                paramsBuilder.putMetadata("payment_subtype", subtype);
                
                // Add additional metadata based on the payment method type
                switch (paymentRequestDTO.getPaymentMethodType()) {
                    case GOOGLE_PAY:
                    case PHONE_PE:
                    case PAYTM:
                    case BHIM:
                    case AMAZON_PAY:
                        if (paymentRequestDTO.getUpiId() != null) {
                            paramsBuilder.putMetadata("upi_id", paymentRequestDTO.getUpiId());
                        }
                        paramsBuilder.putMetadata("upi_provider", subtype);
                        break;
                        
                    case HDFC:
                    case SBI:
                    case ICICI:
                    case AXIS:
                        paramsBuilder.putMetadata("bank_code", subtype);
                        break;
                        
                    default:
                        // No additional metadata needed
                        break;
                }
            }
            // Fall back to the general payment method if specific type not provided
            else if (paymentRequestDTO.getPaymentMethod() != null) {
                // Configure payment method based on type
                switch (paymentRequestDTO.getPaymentMethod()) {
                    case UPI:
                        // For UPI, we need to add specific payment method types
                        paramsBuilder.addPaymentMethodType("upi");
                        // Add UPI-specific metadata if available
                        if (paymentRequestDTO.getUpiId() != null) {
                            paramsBuilder.putMetadata("upi_id", paymentRequestDTO.getUpiId());
                        }
                        if (paymentRequestDTO.getUpiProvider() != null) {
                            paramsBuilder.putMetadata("upi_provider", paymentRequestDTO.getUpiProvider());
                        }
                        break;
                    case CREDIT_CARD:
                        paramsBuilder.addPaymentMethodType("card");
                        paramsBuilder.putMetadata("card_type", "credit");
                        break;
                    case DEBIT_CARD:
                        paramsBuilder.addPaymentMethodType("card");
                        paramsBuilder.putMetadata("card_type", "debit");
                        break;
                    case NETBANKING:
                        paramsBuilder.addPaymentMethodType("netbanking");
                        // Add bank-specific data if available
                        if (paymentRequestDTO.getBankCode() != null) {
                            paramsBuilder.putMetadata("bank_code", paymentRequestDTO.getBankCode());
                        }
                        break;
                    case WALLET:
                        paramsBuilder.addPaymentMethodType("wallet");
                        break;
                    case PAYPAL:
                        paramsBuilder.addPaymentMethodType("paypal");
                        break;
                    case BANK_TRANSFER:
                        paramsBuilder.addPaymentMethodType("bank_transfer");
                        break;
                    case CRYPTO:
                        // Stripe may not support crypto directly
                        paramsBuilder.putMetadata("payment_type", "crypto");
                        break;
                    default:
                        // Default to card
                        paramsBuilder.addPaymentMethodType("card");
                }
            } else {
                // If no payment method specified, allow all commonly used types
                paramsBuilder.addPaymentMethodType("card")
                             .addPaymentMethodType("upi")
                             .addPaymentMethodType("netbanking")
                             .addPaymentMethodType("wallet");
            }
            
            // Set customer if available
            if (paymentRequestDTO.getUserId() != null) {
                paramsBuilder.setCustomer(paymentRequestDTO.getUserId().toString());
            }

            PaymentIntentCreateParams params = paramsBuilder.build();
            return PaymentIntent.create(params);

        } catch (StripeException e) {
            log.error("Error creating payment intent", e);
            throw new RuntimeException("Error creating payment intent", e);
        }
    }

    // For backward compatibility
    public PaymentIntent createPaymentIntent(Long amount, String currency, String description, String userId, Map<String, String> metadata) {
        if (!stripeAvailable) {
            log.warn("Stripe API not available, returning simulated PaymentIntent");
            return createMockPaymentIntent(amount, currency, description);
        }
        
        try {
            log.info("Creating payment intent for amount: {}, currency: {}", amount, currency);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency(currency)
                    .setDescription(description)
                .putAllMetadata(metadata)
                .setCustomer(userId)
                .build();

            return PaymentIntent.create(params);
            
        } catch (StripeException e) {
            log.error("Error creating payment intent", e);
            throw new RuntimeException("Error creating payment intent", e);
        }
    }
    
    // Create a mock PaymentIntent for testing when Stripe API is not available
    private PaymentIntent createMockPaymentIntent(Long amount, String currency, String description) {
        log.info("Creating mock PaymentIntent for amount: {}, currency: {}", amount, currency);
        
        try {
            // Create a minimal PaymentIntent via reflection to avoid NullPointerExceptions
            PaymentIntent mockIntent = new PaymentIntent();
            
            // Use reflection to set the required fields since PaymentIntent doesn't have setters
            java.lang.reflect.Field idField = PaymentIntent.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(mockIntent, "pi_mock_" + UUID.randomUUID().toString().substring(0, 8));
            
            java.lang.reflect.Field amountField = PaymentIntent.class.getDeclaredField("amount");
            amountField.setAccessible(true);
            amountField.set(mockIntent, amount);
            
            java.lang.reflect.Field currencyField = PaymentIntent.class.getDeclaredField("currency");
            currencyField.setAccessible(true);
            currencyField.set(mockIntent, currency);
            
            java.lang.reflect.Field clientSecretField = PaymentIntent.class.getDeclaredField("clientSecret");
            clientSecretField.setAccessible(true);
            clientSecretField.set(mockIntent, "mock_secret_" + UUID.randomUUID().toString());
            
            java.lang.reflect.Field statusField = PaymentIntent.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(mockIntent, "succeeded");
            
            return mockIntent;
        } catch (Exception e) {
            log.error("Failed to create mock PaymentIntent", e);
            // If reflection fails, create a Map-based mock
            log.warn("Returning a simple Map-based mock PaymentIntent");
            return new PaymentIntent() {
                @Override
                public String getId() {
                    return "pi_mock_" + UUID.randomUUID().toString().substring(0, 8);
                }
                
                @Override
                public Long getAmount() {
                    return amount;
                }
                
                @Override
                public String getCurrency() {
                    return currency;
                }
                
                @Override
                public String getClientSecret() {
                    return "mock_secret_" + UUID.randomUUID().toString();
                }
                
                @Override
                public String getStatus() {
                    return "succeeded";
                }
            };
        }
    }

    public String createSetupIntent(String userId, Map<String, String> metadata) {
        if (!stripeAvailable) {
            log.warn("Stripe API not available, returning simulated SetupIntent ID");
            return "seti_" + UUID.randomUUID().toString().replace("-", "");
        }
        
        try {
            log.info("Creating setup intent for user: {}", userId);

            com.stripe.param.SetupIntentCreateParams params = com.stripe.param.SetupIntentCreateParams.builder()
                .setCustomer(userId)
                    .putAllMetadata(metadata)
                    .build();

            com.stripe.model.SetupIntent setupIntent = com.stripe.model.SetupIntent.create(params);
            return setupIntent.getId();
        } catch (StripeException e) {
            log.error("Error creating setup intent", e);
            throw new RuntimeException("Error creating setup intent", e);
        }
    }

    public com.stripe.model.Refund createRefund(String paymentIntentId, long amount, com.stripe.param.RefundCreateParams.Reason reason, Map<String, String> metadata) {
        try {
            log.info("Creating refund for payment: {}, amount: {}", paymentIntentId, amount);
            
            com.stripe.param.RefundCreateParams params = com.stripe.param.RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                .setAmount(amount)
                .setReason(reason)
                .putAllMetadata(metadata)
                .build();

            return com.stripe.model.Refund.create(params);
        } catch (StripeException e) {
            log.error("Error creating refund", e);
            throw new RuntimeException("Error creating refund", e);
        }
    }

    public com.stripe.model.Refund createRefund(String paymentIntentId, long amount, String reasonStr, Map<String, String> metadata) {
        try {
            com.stripe.param.RefundCreateParams.Reason reason = com.stripe.param.RefundCreateParams.Reason.valueOf(reasonStr.toUpperCase());
            return createRefund(paymentIntentId, amount, reason, metadata);
        } catch (IllegalArgumentException e) {
            log.error("Invalid refund reason: {}", reasonStr);
            return createRefund(paymentIntentId, amount, com.stripe.param.RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER, metadata);
        }
    }

    @Transactional
    public String getOrCreateCustomerId(String userId, String email, String name) {
        log.info("Getting or creating customer for user: {}", userId);

        if (!stripeAvailable) {
            log.warn("Stripe API not available, returning simulated customer ID");
            return "cus_" + UUID.randomUUID().toString().replace("-", "");
        }
        
        return paymentCustomerRepository.findByUserId(userId)
            .map(PaymentCustomer::getStripeCustomerId)
                .orElseGet(() -> {
                    try {
                        Map<String, Object> customerParams = new HashMap<>();
                        customerParams.put("email", email);
                        customerParams.put("name", name);
                        customerParams.put("metadata", Map.of("userId", userId));
                        
                    Customer customer = Customer.create(customerParams);
                    String customerId = customer.getId();
                        
                        PaymentCustomer paymentCustomer = new PaymentCustomer();
                        paymentCustomer.setUserId(userId);
                        paymentCustomer.setEmail(email);
                        paymentCustomer.setName(name);
                    paymentCustomer.setStripeCustomerId(customerId);
                        paymentCustomer.setCreatedAt(LocalDateTime.now());
                        paymentCustomer.setUpdatedAt(LocalDateTime.now());
                        paymentCustomerRepository.save(paymentCustomer);
                        
                    return customerId;
                    } catch (StripeException e) {
                        log.error("Error creating Stripe customer", e);
                    throw new PaymentException("Failed to create customer: " + e.getMessage());
                    }
                });
    }
    
    public String savePaymentMethod(String customerId, String stripeToken, String stripePaymentMethodId) {
        if (!stripeAvailable) {
            log.warn("Stripe API not available, returning simulated payment method ID");
            return "pm_" + UUID.randomUUID().toString().replace("-", "");
        }
        
        try {
            log.info("Saving payment method for customer: {}", customerId);
            
            if (stripePaymentMethodId != null && !stripePaymentMethodId.isEmpty()) {
                com.stripe.model.PaymentMethod paymentMethod = com.stripe.model.PaymentMethod.retrieve(stripePaymentMethodId);
                Map<String, Object> params = new HashMap<>();
                params.put("customer", customerId);
                paymentMethod.attach(params);
                return stripePaymentMethodId;
            }
            
            if (stripeToken != null && !stripeToken.isEmpty()) {
                Map<String, Object> params = new HashMap<>();
                params.put("source", stripeToken);
                
                Customer customer = Customer.retrieve(customerId);
                PaymentSource source = customer.getSources().create(params);
                return source.getId();
            }
            
            throw new PaymentException("Either stripeToken or stripePaymentMethodId must be provided");
        } catch (StripeException e) {
            log.error("Error saving payment method", e);
            throw new PaymentException("Failed to save payment method: " + e.getMessage());
        }
    }

    /**
     * Validate webhook signature
     * 
     * @param payload The raw request payload
     * @param sigHeader The Stripe signature header
     * @return The validated Stripe event or null if validation fails
     */
    public com.stripe.model.Event validateWebhookSignature(String payload, String sigHeader) {
        if (stripeProperties.getWebhookSecret() == null || "whsec_dummy".equals(stripeProperties.getWebhookSecret())) {
            log.warn("Using dummy webhook secret. Webhook validation will not work properly.");
            return null;
        }
        
        try {
            return com.stripe.net.Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());
        } catch (com.stripe.exception.SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature", e);
            return null;
        }
    }
}
