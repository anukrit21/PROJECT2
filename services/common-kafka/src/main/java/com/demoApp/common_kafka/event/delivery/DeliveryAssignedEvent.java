package com.demoApp.common_kafka.event.delivery;

import com.demoApp.common_kafka.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DeliveryAssignedEvent extends BaseEvent {

    private UUID deliveryId;
    private UUID orderId;
    private UUID userId;
    private UUID deliveryPersonId;
    private UUID messId;
    private String deliveryStatus; // ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED
    private LocalDateTime assignedTime;
    private LocalDateTime estimatedDeliveryTime;
    private DeliveryAddress deliveryAddress;
    private String specialInstructions;

    /**
     * Custom constructor with initialization logic
     */
    public DeliveryAssignedEvent(UUID deliveryId, UUID orderId, UUID userId, UUID deliveryPersonId,
                                 UUID messId, String deliveryStatus, LocalDateTime assignedTime,
                                 LocalDateTime estimatedDeliveryTime, DeliveryAddress deliveryAddress,
                                 String specialInstructions) {
        super();
        init("DELIVERY_ASSIGNED", "delivery-service");
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.userId = userId;
        this.deliveryPersonId = deliveryPersonId;
        this.messId = messId;
        this.deliveryStatus = deliveryStatus;
        this.assignedTime = assignedTime;
        this.estimatedDeliveryTime = estimatedDeliveryTime;
        this.deliveryAddress = deliveryAddress;
        this.specialInstructions = specialInstructions;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class DeliveryAddress {
        private UUID addressId;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String landmark;
        private Double latitude;
        private Double longitude;

        public DeliveryAddress(UUID addressId, String addressLine1, String addressLine2, String city,
                               String state, String postalCode, String landmark,
                               Double latitude, Double longitude) {
            this.addressId = addressId;
            this.addressLine1 = addressLine1;
            this.addressLine2 = addressLine2;
            this.city = city;
            this.state = state;
            this.postalCode = postalCode;
            this.landmark = landmark;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
