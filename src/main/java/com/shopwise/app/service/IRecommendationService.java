package com.shopwise.app.service;

import java.util.List;

import com.shopwise.app.dto.response.RecommendationResponse;

public interface IRecommendationService {

    List<RecommendationResponse> recommend(Long productId, int limit);
}