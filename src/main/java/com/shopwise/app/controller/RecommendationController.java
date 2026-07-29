package com.shopwise.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopwise.app.dto.response.RecommendationResponse;
import com.shopwise.app.service.RecommendationService;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<List<RecommendationResponse>> recommend(
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "5") int limit) {
        List<RecommendationResponse> recommendations = recommendationService.recommend(productId, limit);
        return ResponseEntity.ok(recommendations);
    }
}
