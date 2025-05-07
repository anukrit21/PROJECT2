package com.demoApp.delivery.controller;

import com.demoApp.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/tracking")
@RequiredArgsConstructor
@Slf4j
public class TrackingViewController {

    private final DeliveryRepository deliveryRepository;

    /**
     * Serve the tracking page for a delivery
     */
    @GetMapping("/{deliveryId}")
    public ModelAndView trackDelivery(@PathVariable Long deliveryId) {
        try {
            // Validate delivery exists
            deliveryRepository.findById(deliveryId)
                    .orElseThrow(() -> new RuntimeException("Delivery not found with ID: " + deliveryId));

            // Create ModelAndView and add deliveryId as a request parameter
            ModelAndView modelAndView = new ModelAndView("forward:/map.html");
            modelAndView.addObject("deliveryId", deliveryId);

            log.info("Serving tracking page for delivery ID: {}", deliveryId);
            return modelAndView;
        } catch (Exception e) {
            log.error("Error serving tracking page for delivery ID {}: {}", deliveryId, e.getMessage());

            // Handle error case - show error page
            ModelAndView errorView = new ModelAndView("forward:/error.html");
            errorView.addObject("errorMessage", "Delivery not found or error occurred: " + e.getMessage());
            return errorView;
        }
    }
}
