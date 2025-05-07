package com.demoApp.payment.service;

import com.demoApp.payment.dto.PaymentMethodDTO;
import com.demoApp.payment.entity.PaymentMethod;
import com.demoApp.payment.exception.PaymentException;
import com.demoApp.payment.exception.ResourceNotFoundException;
import com.demoApp.payment.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final StripeService stripeService;
    private final ModelMapper modelMapper;

    /**
     * Save a new payment method
     */
    @Transactional
    public PaymentMethodDTO savePaymentMethod(PaymentMethodDTO dto) {
        log.info("Saving payment method for user: {}", dto.getUserId());
        try {
            // Check if Stripe customer exists
            String stripeCustomerId = dto.getStripeCustomerId();
            if (stripeCustomerId == null || stripeCustomerId.isEmpty()) {
                stripeCustomerId = stripeService.getOrCreateCustomerId(
                        dto.getUserId().toString(),
                        dto.getEmail(),
                        dto.getName()
                );
            }

            // Create or attach payment method to customer
            String stripePaymentMethodId = stripeService.savePaymentMethod(
                    stripeCustomerId,
                    dto.getStripeToken(),
                    dto.getStripePaymentMethodId()
            );

            // Create payment method entity
            PaymentMethod paymentMethod = new PaymentMethod();
            if (dto.getId() != null) {
                paymentMethod = paymentMethodRepository.findById(dto.getId())
                        .orElse(new PaymentMethod());
            }

            paymentMethod.setUserId(dto.getUserId());
            paymentMethod.setType(dto.getType());
            paymentMethod.setLastFour(dto.getLastFour());
            paymentMethod.setExpiryMonth(dto.getExpiryMonth());
            paymentMethod.setExpiryYear(dto.getExpiryYear());
            paymentMethod.setBrand(dto.getBrand());
            paymentMethod.setStripePaymentMethodId(stripePaymentMethodId);
            paymentMethod.setStripeCustomerId(stripeCustomerId);

            // Set default if requested
            if (Boolean.TRUE.equals(dto.getIsDefault())) {
                resetDefaultPaymentMethods(dto.getUserId());
                paymentMethod.setIsDefault(true);
            } else if (paymentMethod.getId() == null && paymentMethodRepository.countByUserIdAndIsDefaultTrue(dto.getUserId()) == 0) {
                // First payment method is default
                paymentMethod.setIsDefault(true);
            }

            paymentMethod.setIsActive(true);
            
            LocalDateTime now = LocalDateTime.now();
            if (paymentMethod.getCreatedAt() == null) {
                paymentMethod.setCreatedAt(now);
            }
            paymentMethod.setUpdatedAt(now);

            paymentMethod = paymentMethodRepository.save(paymentMethod);
            return modelMapper.map(paymentMethod, PaymentMethodDTO.class);
        } catch (Exception e) {
            log.error("Error saving payment method", e);
            throw new PaymentException("Failed to save payment method: " + e.getMessage());
        }
    }

    /**
     * Set a payment method as default
     */
    @Transactional
    public PaymentMethodDTO setDefaultPaymentMethod(Long id) {
        log.info("Setting payment method as default: {}", id);
        try {
            PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with ID: " + id));

            // Reset all other payment methods
            resetDefaultPaymentMethods(paymentMethod.getUserId());

            // Set this one as default
            paymentMethod.setIsDefault(true);
            paymentMethod.setUpdatedAt(LocalDateTime.now());
            paymentMethod = paymentMethodRepository.save(paymentMethod);
            
            return modelMapper.map(paymentMethod, PaymentMethodDTO.class);
        } catch (Exception e) {
            log.error("Error setting default payment method", e);
            throw new PaymentException("Failed to set default payment method: " + e.getMessage());
        }
    }

    /**
     * Delete a payment method (soft delete)
     */
    @Transactional
    public void deletePaymentMethod(Long id) {
        log.info("Deleting payment method: {}", id);
        try {
            PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment method not found with ID: " + id));

            // Soft delete
            paymentMethod.setIsActive(false);
            paymentMethod.setUpdatedAt(LocalDateTime.now());
            paymentMethodRepository.save(paymentMethod);

            // If this was default, set another one as default
            if (Boolean.TRUE.equals(paymentMethod.getIsDefault())) {
                paymentMethodRepository.findFirstByUserIdAndIsActiveTrueOrderByCreatedAtDesc(paymentMethod.getUserId())
                        .ifPresent(pm -> {
                            pm.setIsDefault(true);
                            pm.setUpdatedAt(LocalDateTime.now());
                            paymentMethodRepository.save(pm);
                        });
            }
        } catch (Exception e) {
            log.error("Error deleting payment method", e);
            throw new PaymentException("Failed to delete payment method: " + e.getMessage());
        }
    }

    /**
     * Get payment methods for a user
     */
    public List<PaymentMethodDTO> getPaymentMethodsByUserId(Long userId) {
        log.info("Getting payment methods for user: {}", userId);
        List<PaymentMethod> methods = paymentMethodRepository.findByUserIdAndIsActiveTrue(userId);
        return methods.stream()
                .map(method -> modelMapper.map(method, PaymentMethodDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Reset all default payment methods for a user
     */
    private void resetDefaultPaymentMethods(Long userId) {
        paymentMethodRepository.findByUserIdAndIsDefaultTrue(userId)
                .forEach(pm -> {
                    pm.setIsDefault(false);
                    pm.setUpdatedAt(LocalDateTime.now());
                    paymentMethodRepository.save(pm);
                });
    }
} 