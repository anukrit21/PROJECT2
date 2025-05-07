package com.demoApp.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.demoApp.payment.config.StripeConfig;
import com.demoApp.payment.dto.ApiResponse;
import com.demoApp.payment.dto.PaymentRequestDTO;
import com.demoApp.payment.dto.PaymentResponseDTO;
import com.demoApp.payment.service.PaymentService;
import com.demoApp.payment.service.StripeService;
import com.stripe.model.PaymentIntent;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for payment checkout UI
 */
@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
@Slf4j
public class PaymentCheckoutController {

    private final PaymentService paymentService;
    private final StripeService stripeService;
    private final StripeConfig stripeConfig;

    /**
     * Render the checkout page with payment details
     */
    @GetMapping("/{orderId}")
    public String getCheckoutPage(
            @PathVariable String orderId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false, defaultValue = "INR") String currency,
            @RequestParam(required = false) String description,
            Model model,
            Authentication authentication) {

        try {
            // Validate user is authenticated
            if (authentication == null) {
                return "redirect:/login?checkout=true&orderId=" + orderId;
            }

            String userId = authentication.getName();

            // Prepare display data
            Map<String, Object> checkoutData = new HashMap<>();
            checkoutData.put("orderId", orderId);
            checkoutData.put("amount", amount);
            checkoutData.put("currency", currency);
            checkoutData.put("description", description != null ? description : "Order: " + orderId);
            checkoutData.put("userId", userId);
            
            // Add available payment methods
            checkoutData.put("supportsCreditCard", true);
            checkoutData.put("supportsDebitCard", true);
            checkoutData.put("supportsUpi", true);
            checkoutData.put("supportsNetbanking", true);
            checkoutData.put("supportsWallet", true);

            model.addAttribute("checkoutData", checkoutData);
            model.addAttribute("stripePublicKey", stripeConfig.getPublishableKey());

            return "checkout";
        } catch (Exception e) {
            log.error("Error rendering checkout page", e);
            model.addAttribute("error", "There was an error preparing the checkout page");
            return "error";
        }
    }

    /**
     * Create payment intent for checkout
     */
    @PostMapping("/create-payment-intent")
    @ResponseBody
    public ResponseEntity<?> createPaymentIntent(@Valid @RequestBody PaymentRequestDTO paymentRequestDTO,
                                                 Authentication authentication) {
        try {
            // Security check
            String userId = authentication.getName();
            if (!userId.equals(paymentRequestDTO.getUserId().toString())) {
                return ResponseEntity.badRequest().body(new ApiResponse(
                        false,
                        "Unauthorized user",
                        null));
            }

            // Convert amount to paisa/cents (multiply by 100)
            Long amountInSmallestUnit = paymentRequestDTO.getAmount()
                    .multiply(new BigDecimal("100"))
                    .longValue();

            // Update amount in smallest unit back in the DTO (if needed)
            paymentRequestDTO.setAmount(new BigDecimal(amountInSmallestUnit));

            // Call StripeService to create payment intent with DTO
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(paymentRequestDTO);

            // Convert PaymentIntent to PaymentResponseDTO
            PaymentResponseDTO response = new PaymentResponseDTO();
            response.setClientSecret(paymentIntent != null ? paymentIntent.getClientSecret() : "mock_secret");
            response.setPaymentIntentId(paymentIntent != null ? paymentIntent.getId() : "mock_id");

            return ResponseEntity.ok(Map.of(
                    "clientSecret", response.getClientSecret(),
                    "amount", amountInSmallestUnit,
                    "currency", paymentRequestDTO.getCurrency(),
                    "paymentId", response.getPaymentIntentId()
            ));
        } catch (Exception e) {
            log.error("Error creating payment intent", e);
            return ResponseEntity.badRequest().body(new ApiResponse(
                    false,
                    "Payment processing error: " + e.getMessage(),
                    null));
        }
    }

    /**
     * Handle successful payment
     */
    @GetMapping("/success")
    public String paymentSuccess(
            @RequestParam String paymentId,
            Model model) {

        try {
            PaymentResponseDTO payment = paymentService.getPaymentById(paymentId);
            model.addAttribute("payment", payment);
            return "payment-success";
        } catch (Exception e) {
            log.error("Error processing successful payment", e);
            model.addAttribute("error", "There was an error processing your payment confirmation");
            return "error";
        }
    }

    /**
     * Handle payment cancellation
     */
    @GetMapping("/cancel")
    public String paymentCancel(
            @RequestParam String paymentId,
            Model model) {

        model.addAttribute("paymentId", paymentId);
        model.addAttribute("message", "Your payment was canceled. You can try again or contact support.");
        return "payment-cancel";
    }
} 