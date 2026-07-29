package com.shopwise.app.service;

import java.util.List;

import com.shopwise.app.dto.response.RecommendationResponse;

public interface RecommendationService {

    List<RecommendationResponse> recommend(Long productId, int limit);
}
