package com.demoApp.common_kafka.event.mess;

import com.demoApp.common_kafka.event.BaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MessCreatedEvent extends BaseEvent {

    private UUID messId;
    private UUID ownerId;
    private String name;
    private String description;
    private String phoneNumber;
    private String emailAddress;
    private BigDecimal rating;
    private boolean verified;
    private boolean active;
    private List<MessAddress> addresses;
    private List<OperatingHours> operatingHours;
    private List<String> cuisineTypes;
    private List<String> amenities;
    private MessPricingInfo pricingInfo;

    /**
     * Custom constructor with initialization
     */
    public MessCreatedEvent(UUID messId, UUID ownerId, String name, String description,
                            String phoneNumber, String emailAddress, BigDecimal rating,
                            boolean verified, boolean active, List<MessAddress> addresses,
                            List<OperatingHours> operatingHours, List<String> cuisineTypes,
                            List<String> amenities, MessPricingInfo pricingInfo) {
        super();
        init("MESS_CREATED", "mess-service");
        this.messId = messId;
        this.ownerId = ownerId;
        this.name = name;
        this.description = description;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.rating = rating;
        this.verified = verified;
        this.active = active;
        this.addresses = addresses;
        this.operatingHours = operatingHours;
        this.cuisineTypes = cuisineTypes;
        this.amenities = amenities;
        this.pricingInfo = pricingInfo;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class MessAddress {
        private UUID addressId;
        private String type; // PRIMARY, BRANCH
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String landmark;
        private Double latitude;
        private Double longitude;

        public MessAddress(UUID addressId, String type, String addressLine1, String addressLine2,
                           String city, String state, String postalCode, String landmark,
                           Double latitude, Double longitude) {
            this.addressId = addressId;
            this.type = type;
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

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class OperatingHours {
        private String dayOfWeek;
        private LocalTime breakfastStart;
        private LocalTime breakfastEnd;
        private LocalTime lunchStart;
        private LocalTime lunchEnd;
        private LocalTime dinnerStart;
        private LocalTime dinnerEnd;
        private boolean closed;

        public OperatingHours(String dayOfWeek, LocalTime breakfastStart, LocalTime breakfastEnd,
                              LocalTime lunchStart, LocalTime lunchEnd,
                              LocalTime dinnerStart, LocalTime dinnerEnd, boolean closed) {
            this.dayOfWeek = dayOfWeek;
            this.breakfastStart = breakfastStart;
            this.breakfastEnd = breakfastEnd;
            this.lunchStart = lunchStart;
            this.lunchEnd = lunchEnd;
            this.dinnerStart = dinnerStart;
            this.dinnerEnd = dinnerEnd;
            this.closed = closed;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class MessPricingInfo {
        private BigDecimal breakfastPrice;
        private BigDecimal lunchPrice;
        private BigDecimal dinnerPrice;
        private BigDecimal monthlySubscriptionPrice;

        public MessPricingInfo(BigDecimal breakfastPrice, BigDecimal lunchPrice,
                               BigDecimal dinnerPrice, BigDecimal monthlySubscriptionPrice) {
            this.breakfastPrice = breakfastPrice;
            this.lunchPrice = lunchPrice;
            this.dinnerPrice = dinnerPrice;
            this.monthlySubscriptionPrice = monthlySubscriptionPrice;
        }
    }
}
