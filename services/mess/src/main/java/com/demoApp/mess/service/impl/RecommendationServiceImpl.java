package com.demoApp.mess.service.impl;

import com.demoApp.mess.dto.RecommendationResponse;
import com.demoApp.mess.entity.Mess;
import com.demoApp.mess.ml.recommendation.MessRecommendationModel;
import com.demoApp.mess.repository.MessRepository;
import com.demoApp.mess.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final MessRepository messRepository;
    private final MessRecommendationModel messRecommendationModel;

    @Override
    public List<RecommendationResponse> getMessRecommendations(String category, String cuisine) {
        // Normalize input parameters for null or "All" values
        String effectiveCategory = (category == null || category.equalsIgnoreCase("All")) ? "All" : category;
        String effectiveCuisine = (cuisine == null || cuisine.equalsIgnoreCase("All")) ? "All" : cuisine;
        
        log.info("Fetching recommendations for category: {}, cuisine: {}", effectiveCategory, effectiveCuisine);

        MessRecommendationModel.RecommendationResponse modelResponse = 
            messRecommendationModel.recommendMesses(effectiveCategory, effectiveCuisine);
        
        if (modelResponse == null || modelResponse.getRecommended() == null) {
            log.warn("No recommendations found");
            return Collections.emptyList();
        }

        List<RecommendationResponse> recommendations = processRecommendations(modelResponse.getRecommended());
        
        // Set the correct category and cuisine in the response
        if (!effectiveCategory.equalsIgnoreCase("All") || !effectiveCuisine.equalsIgnoreCase("All")) {
            recommendations.forEach(rec -> {
                if (!effectiveCategory.equalsIgnoreCase("All")) {
                    rec.setCategory(effectiveCategory);
                }
                if (!effectiveCuisine.equalsIgnoreCase("All")) {
                    rec.setCuisine(effectiveCuisine);
                }
            });
        }
        
        return recommendations;
    }

    private List<RecommendationResponse> processRecommendations(
            List<MessRecommendationModel.RecommendedMessDto> recommendations) {
        
        List<Long> messIds = recommendations.stream()
            .map(rec -> Long.valueOf(rec.getMessId()))
            .collect(Collectors.toList());

        Map<Long, Mess> activeMesses = messRepository.findAllById(messIds).stream()
            .filter(Mess::isActive)
            .collect(Collectors.toMap(Mess::getId, Function.identity()));

        return recommendations.stream()
            .filter(rec -> activeMesses.containsKey(Long.valueOf(rec.getMessId())))
            .map(rec -> buildRecommendationResponse(rec, activeMesses))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private RecommendationResponse buildRecommendationResponse(
            MessRecommendationModel.RecommendedMessDto rec, 
            Map<Long, Mess> messMap) {
        
        // Convert string id to Long
        Long messId = Long.valueOf(rec.getMessId());
        Mess mess = messMap.get(messId);
        
        if (mess == null) {
            return null;
        }

        String recommendedItemsStr = rec.getRecommendedItems();
        String[] recommendedItems = recommendedItemsStr != null && !recommendedItemsStr.isBlank() 
            ? recommendedItemsStr.split("\n") 
            : new String[0];
            
        // Convert string rating to double if needed
        double score = parseRating(rec.getRating());
        // If score is 0 and mess has a rating, use that instead
        if (score == 0.0 && mess.getRating() != null && !mess.getRating().isBlank()) {
            try {
                score = Double.parseDouble(mess.getRating());
            } catch (NumberFormatException e) {
                log.warn("Could not parse rating from mess: {}", mess.getRating());
            }
        }

        // Ensure score has a value to avoid null pointer exceptions in the frontend
        if (Double.isNaN(score) || Double.isInfinite(score)) {
            score = 3.0; // Default to average score for problematic values
        }

        return RecommendationResponse.builder()
            .messId(messId)
            .messName(mess.getName() != null ? mess.getName() : "")
            .category("All") // Will be overridden in the main method if needed
            .cuisine(mess.getCuisineType() != null ? mess.getCuisineType() : "All")
            .score(score)
            .address(mess.getAddress() != null ? mess.getAddress() : "")
            .location(mess.getLocation() != null ? mess.getLocation() : "")
            .recommendedItems(recommendedItems)
            .build();
    }

    private double parseRating(Object rating) {
        if (rating instanceof Number) {
            return ((Number) rating).doubleValue();
        }
        if (rating instanceof String) {
            return parseRatingFromString((String) rating);
        }
        log.warn("Unknown rating type: {}", rating != null ? rating.getClass().getSimpleName() : "null");
        return 0.0;
    }

    private double parseRatingFromString(String ratingStr) {
        try {
            if (ratingStr.contains("(")) {
                int start = ratingStr.lastIndexOf("(") + 1;
                int end = ratingStr.indexOf(")", start);
                return Double.parseDouble(ratingStr.substring(start, end));
            }
            return Double.parseDouble(ratingStr);
        } catch (Exception e) {
            log.error("Failed to parse rating: {}", ratingStr, e);
            return 0.0;
        }
    }
}