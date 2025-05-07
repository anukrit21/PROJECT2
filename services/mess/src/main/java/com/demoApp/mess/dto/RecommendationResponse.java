package com.demoApp.mess.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private Long messId;
    private String messName;
    private String category;
    private String cuisine;
    private Double score;
    private String address;
    private String location;
    private String[] recommendedItems;
}
