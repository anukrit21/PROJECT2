package com.demoApp.owner.repository;

import com.demoApp.owner.dto.OrderDTO;
import com.demoApp.owner.entity.Order;
import com.demoApp.owner.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByOwner(Owner owner);
    List<Order> findByOwnerAndStatus(Owner owner, Order.OrderStatus status);
    List<Order> findByOwnerAndOrderDateBetween(Owner owner, LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByStatus(Order.OrderStatus status);
    Collection<OrderDTO> findByOwnerId(Long ownerId);
}