package com.demoApp.common_kafka.event.review;

import com.demoApp.common_kafka.event.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReviewSubmittedEvent extends BaseEvent {
    
    private UUID reviewId;
    private UUID userId;
    private UUID messId;
    private UUID orderId;
    private LocalDateTime submittedAt;
    private BigDecimal rating;
    private String reviewText;
    private String reviewTitle;
    private boolean anonymous;
    private boolean verified;
    private String status; // PENDING, APPROVED, REJECTED
    private List<String> tags;
    private List<ReviewMedia> media;

       
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewMedia {
        private UUID mediaId;
        private String mediaType; 
        private String mediaUrl;
        private String caption;
    }
}
