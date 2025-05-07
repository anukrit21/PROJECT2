package com.demoApp.mess.service;

import com.demoApp.mess.dto.RecommendationResponse;
import java.util.List;

public interface RecommendationService {
    List<RecommendationResponse> getMessRecommendations(String category, String cuisine);
} 