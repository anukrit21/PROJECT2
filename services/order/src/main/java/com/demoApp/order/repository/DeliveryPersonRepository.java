package com.demoApp.order.repository;

import com.demoApp.order.entity.DeliveryPerson;
import com.demoApp.order.entity.DeliveryPersonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface DeliveryPersonRepository extends JpaRepository<DeliveryPerson, Long> {
    List<DeliveryPerson> findByStatusAndIsAvailableAndShiftStartTimeLessThanEqualAndShiftEndTimeGreaterThanEqual(
        DeliveryPersonStatus status,
        Boolean isAvailable,
        LocalTime currentTime,
        LocalTime currentTimeEnd
    );
} 