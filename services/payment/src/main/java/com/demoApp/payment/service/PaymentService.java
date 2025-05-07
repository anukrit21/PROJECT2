package com.demoApp.payment.service;

import com.demoApp.payment.dto.PaymentRequestDTO;
import com.demoApp.payment.dto.PaymentResponseDTO;
import com.demoApp.payment.dto.RefundRequestDTO;
import com.demoApp.payment.entity.Payment;
import com.demoApp.payment.exception.PaymentException;
import com.demoApp.payment.exception.ResourceNotFoundException;
import com.demoApp.payment.model.PaymentStatus;
import com.demoApp.payment.repository.PaymentRepository;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final ModelMapper modelMapper;

    /**
     * Process a payment.
     */
    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO dto) {
        log.info("Processing payment for user ID: {}, amount: {}", dto.getUserId(), dto.getAmount());
        try {
            // Create initial payment record in DB
            Payment payment = createPaymentRecord(dto);

            // Create Stripe PaymentIntent
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(dto);

            // Update payment record with payment intent details
            payment.setPaymentProviderReference(paymentIntent.getId());
            payment.setPaymentProviderMethod("stripe");
            payment.setStatus(PaymentStatus.PENDING);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Build response
            PaymentResponseDTO response = modelMapper.map(payment, PaymentResponseDTO.class);
            response.setClientSecret(paymentIntent.getClientSecret());
            response.setRequiresAction("requires_action".equals(paymentIntent.getStatus()));

            log.info("Payment processed successfully. Payment ID: {}", payment.getPaymentId());
            return response;
        } catch (Exception e) {
            log.error("Error processing payment", e);
            throw new PaymentException("Error processing payment: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponseDTO createPaymentSetup(PaymentRequestDTO dto) {
        log.info("Creating payment setup for user ID: {}", dto.getUserId());
        try {
            Map<String, String> metadataMap = new HashMap<>();
            if (dto.getMetadata() != null && !dto.getMetadata().isEmpty()) {
                metadataMap.putAll(dto.getMetadata());
            }
            String setupIntentId = stripeService.createSetupIntent(
                    dto.getUserId().toString(), metadataMap
            );

            PaymentResponseDTO response = new PaymentResponseDTO();
            response.setUserId(dto.getUserId());
            response.setSetupIntentId(setupIntentId);
            log.info("Payment setup created successfully");
            return response;
        } catch (Exception e) {
            log.error("Error creating payment setup", e);
            throw new PaymentException("Error creating payment setup: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponseDTO processRefund(RefundRequestDTO dto) {
        log.info("Processing refund for payment ID: {}, amount: {}", dto.getPaymentId(), dto.getAmount());
        try {
            Payment payment = paymentRepository.findByPaymentId(dto.getPaymentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + dto.getPaymentId()));
            if (payment.getStatus() != PaymentStatus.COMPLETED) {
                throw new PaymentException("Payment cannot be refunded because it is not completed");
            }

            Map<String, String> metadataMap = new HashMap<>();
if (dto.getMetadata() != null && !dto.getMetadata().isEmpty()) {
    // Assuming dto.getMetadata() returns a string with key-value pairs like: "key1=value1;key2=value2"
    String[] entries = dto.getMetadata().split(";");
    for (String entry : entries) {
        String[] keyValue = entry.split("=");
        if (keyValue.length == 2) {
            metadataMap.put(keyValue[0], keyValue[1]);
        }
    }
}

            

            long amountInCents = dto.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

            Refund refund = stripeService.createRefund(
                    payment.getPaymentProviderReference(),
                    amountInCents,
                    dto.getReason(),
                    metadataMap
            );

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setRefundAmount(dto.getAmount());
            payment.setRefundReason(dto.getReason());
            payment.setRefundReference(refund.getId());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            PaymentResponseDTO response = modelMapper.map(payment, PaymentResponseDTO.class);
            log.info("Refund processed successfully. Payment ID: {}, Refund ID: {}", payment.getPaymentId(), refund.getId());
            return response;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing refund", e);
            throw new PaymentException("Error processing refund: " + e.getMessage());
        }
    }

    public PaymentResponseDTO getPaymentById(String id) {
        log.info("Getting payment by ID: {}", id);
        Payment payment = paymentRepository.findByPaymentId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        return modelMapper.map(payment, PaymentResponseDTO.class);
    }

    public Page<PaymentResponseDTO> getPaymentsByUserId(Long userId, Pageable pageable) {
        log.info("Getting payments for user ID: {}", userId);
        Page<Payment> payments = paymentRepository.findByUserId(userId, pageable);
        return payments.map(p -> modelMapper.map(p, PaymentResponseDTO.class));
    }

    public Page<PaymentResponseDTO> getPaymentsByMerchantId(Long merchantId, Pageable pageable) {
        log.info("Getting payments for merchant ID: {}", merchantId);
        Page<Payment> payments = paymentRepository.findByMerchantId(merchantId, pageable);
        return payments.map(p -> modelMapper.map(p, PaymentResponseDTO.class));
    }

    public Page<PaymentResponseDTO> getPaymentsByOwnerId(Long ownerId, Pageable pageable) {
        log.info("Getting payments for owner ID: {}", ownerId);
        Page<Payment> payments = paymentRepository.findByOwnerId(ownerId, pageable);
        return payments.map(p -> modelMapper.map(p, PaymentResponseDTO.class));
    }

    public Page<PaymentResponseDTO> getAllPayments(Pageable pageable) {
        log.info("Getting all payments");
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(p -> modelMapper.map(p, PaymentResponseDTO.class));
    }

    @Transactional
    public PaymentResponseDTO updatePaymentStatus(String paymentId, PaymentStatus status, String failureMessage, String failureCode) {
        log.info("Updating payment status for ID: {} to: {}", paymentId, status);
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        payment.setStatus(status);
        if (failureMessage != null) {
            payment.setFailureMessage(failureMessage);
        }
        if (failureCode != null) {
            payment.setFailureCode(failureCode);
        }
        payment.setUpdatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        return modelMapper.map(payment, PaymentResponseDTO.class);
    }

    public BigDecimal calculateTotalProcessedAmount(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating total processed amount between {} and {}", startDate, endDate);
        return paymentRepository.sumAmountByStatusAndDateRange(
                PaymentStatus.COMPLETED,
                startDate,
                endDate != null ? endDate : LocalDateTime.now()
        );
    }

    private Payment createPaymentRecord(PaymentRequestDTO dto) {
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString());
        payment.setUserId(dto.getUserId());
        payment.setDescription(dto.getDescription());
        payment.setAmount(dto.getAmount());
        payment.setTax(dto.getTax());
        payment.setCurrency(dto.getCurrency().toLowerCase());
        payment.setPaymentMethod(dto.getPaymentMethod());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setOrderReference(dto.getOrderReference());
        payment.setProductId(dto.getProductId());
        payment.setSubscriptionId(dto.getSubscriptionId());
        payment.setMerchantId(dto.getMerchantId());
        payment.setOwnerId(dto.getOwnerId());
        payment.setMetadata(dto.getMetadata());
        payment.setCustomerName(dto.getCustomerName());
        payment.setCustomerEmail(dto.getCustomerEmail());
        payment.setBillingAddress(dto.getBillingAddress());
        payment.setShippingAddress(dto.getShippingAddress());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }
}
