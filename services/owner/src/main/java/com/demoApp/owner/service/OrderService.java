package com.demoApp.owner.service;

import com.demoApp.owner.dto.OrderDTO;
import com.demoApp.owner.entity.MenuItem;
import com.demoApp.owner.entity.Order;
import com.demoApp.owner.entity.Owner;
import com.demoApp.owner.exception.ResourceNotFoundException;
import com.demoApp.owner.repository.MenuItemRepository;
import com.demoApp.owner.repository.OrderRepository;
import com.demoApp.owner.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OwnerRepository ownerRepository;
    private final MenuItemRepository menuItemRepository;
    private final ModelMapper modelMapper;

    public List<OrderDTO> getOrdersByOwner(Long ownerId) {
        if (!ownerRepository.existsById(ownerId)) {
            throw new ResourceNotFoundException("Owner not found with id: " + ownerId);
        }

        return orderRepository.findByOwnerId(ownerId).stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return modelMapper.map(order, OrderDTO.class);
    }

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Owner owner = ownerRepository.findById(orderDTO.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + orderDTO.getOwnerId()));

        Order order = new Order();
        order.setUserId(orderDTO.getUserId());
        order.setOwner(owner);

        List<Order.OrderItem> orderItems = orderDTO.getOrderItems().stream()
                .map(itemDTO -> {
                    MenuItem menuItem = menuItemRepository.findById(itemDTO.getMenuItemId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Menu item not found with id: " + itemDTO.getMenuItemId()));

                    // Set properties directly for OrderItem
                    Order.OrderItem orderItem = new Order.OrderItem();
                    orderItem.setMenuItemId(menuItem.getId());
                    orderItem.setName(menuItem.getName());
                    orderItem.setPrice(menuItem.getPrice());
                    orderItem.setQuantity(itemDTO.getQuantity());
                    orderItem.setItemTotal(menuItem.getPrice().multiply(new BigDecimal(itemDTO.getQuantity())));

                    return orderItem;
                })
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        BigDecimal total = orderItems.stream()
                .map(Order.OrderItem::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);
        return modelMapper.map(savedOrder, OrderDTO.class);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long id, Order.OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        return modelMapper.map(updatedOrder, OrderDTO.class);
    }

    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
}
