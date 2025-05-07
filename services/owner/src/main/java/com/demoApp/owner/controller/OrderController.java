package com.demoApp.owner.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.demoApp.owner.dto.OrderDTO;
import com.demoApp.owner.dto.OrderResponse;
import com.demoApp.owner.entity.Order;
import com.demoApp.owner.service.OrderService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ModelMapper modelMapper;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderDTO orderDTO) {
        return new ResponseEntity<>(
                modelMapper.map(orderService.createOrder(orderDTO), OrderResponse.class),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> responses = orderService.getAllOrders()
                .stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        OrderDTO dto = orderService.getOrderById(id);
        return ResponseEntity.ok(modelMapper.map(dto, OrderResponse.class));
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasRole('OWNER') or #ownerId == authentication.principal.id")
    public ResponseEntity<List<OrderResponse>> getOrdersByOwnerId(@PathVariable Long ownerId) {
        List<OrderResponse> responses = orderService.getOrdersByOwner(ownerId)
                .stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        OrderDTO dto = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(modelMapper.map(dto, OrderResponse.class));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
