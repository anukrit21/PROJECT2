package com.demoApp.order.service;

import com.demoApp.order.entity.DeliveryPerson;
import com.demoApp.order.entity.Order;
import com.demoApp.order.entity.OrderStatus;
import com.demoApp.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final DeliveryAllocationService deliveryAllocationService;

    @Transactional
    public Order createOrder(Order order) {
        order.setOrderId(UUID.randomUUID().toString());
        order.setOrderTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setExpectedDeliveryTime(calculateExpectedDeliveryTime(order.getOrderTime()));
        
        if (order.getIsSubscriptionOrder() == null) {
            order.setIsSubscriptionOrder(false);
        }
        
        return orderRepository.save(order);
    }

    @Transactional
    public Order processOrder(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING state");
        }

        Optional<DeliveryPerson> deliveryPerson = deliveryAllocationService.allocateDeliveryPerson(order);
        
        if (deliveryPerson.isPresent()) {
            order.setDeliveryPerson(deliveryPerson.get());
            order.setStatus(OrderStatus.ACCEPTED);
            log.info("Order {} assigned to delivery person {}", order.getOrderId(), deliveryPerson.get().getId());
        } else {
            log.warn("No delivery person available for order {}", order.getOrderId());
            // You might want to implement a retry mechanism or queue the order
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        // Handle delivery person release when order is completed or cancelled
        if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED) {
            deliveryAllocationService.releaseDeliveryPerson(order);
        }

        order.setStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveryTime(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderWithDeliveryRating(Long id, Double rating) {
        if (rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found: " + id));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot rate delivery person for undelivered order");
        }

        DeliveryPerson deliveryPerson = order.getDeliveryPerson();
        if (deliveryPerson != null) {
            deliveryAllocationService.updateDeliveryPersonRating(deliveryPerson, rating);
        }

        return order;
    }

    private LocalDateTime calculateExpectedDeliveryTime(LocalDateTime orderTime) {
        // Add 45 minutes for preparation and delivery
        return orderTime.plusMinutes(45);
    }
} 