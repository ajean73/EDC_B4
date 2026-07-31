package com.shopwise.app.recommendation;

import java.util.List;

import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.Sale;

public interface RecommendationStrategy {

    List<RecommendationCandidate> recommend(List<Sale> sales, List<Product> catalog, Long referenceProductId, int limit);
}
