package com.shopwise.app.recommendation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.entity.SaleItem;

@Component
public class CoOccurrenceRecommendationStrategy implements RecommendationStrategy {

    private static final double MIN_SUPPORT = 0.05;
    private static final double MIN_CONFIDENCE = 0.15;
    private static final double MIN_LIFT = 1.0;

    @Override
    public List<RecommendationCandidate> recommend(List<Sale> sales, List<Product> catalog, Long referenceProductId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));

        Map<Long, Integer> popularity = computePopularity(sales);
        if (referenceProductId == null) {
            return bestSellers(catalog, popularity, safeLimit);
        }

        if (sales.isEmpty() || popularity.getOrDefault(referenceProductId, 0) == 0) {
            return bestSellersExcluding(catalog, popularity, safeLimit, referenceProductId);
        }

        int transactionCount = Math.max(1, sales.size());
        Map<Long, Integer> supportCountByProduct = computeSupportCountByProduct(sales);
        Map<Long, Map<Long, Integer>> pairSupportCount = computePairSupportCount(sales);
        Map<Long, Double> scores = new HashMap<>();

        for (Product product : catalog) {
            Long candidateId = product.getId();
            if (candidateId.equals(referenceProductId)) {
                continue;
            }

            int leftCount = supportCountByProduct.getOrDefault(referenceProductId, 0);
            int rightCount = supportCountByProduct.getOrDefault(candidateId, 0);
            int pairCount = pairSupportCount.getOrDefault(referenceProductId, Map.of())
                    .getOrDefault(candidateId, 0);

            if (leftCount == 0 || rightCount == 0 || pairCount == 0) {
                continue;
            }

            double support = pairCount / (double) transactionCount;
            double confidence = pairCount / (double) leftCount;
            double probCandidate = rightCount / (double) transactionCount;
            double lift = confidence / Math.max(probCandidate, 1e-9);

            if (support < MIN_SUPPORT || confidence < MIN_CONFIDENCE || lift < MIN_LIFT) {
                continue;
            }

            double score = (0.5 * lift) + (0.3 * confidence) + (0.2 * support);
            double finalScore = roundScore(score);

            if (finalScore > 0) {
                scores.put(candidateId, finalScore);
            }
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(safeLimit)
                .map(entry -> new RecommendationCandidate(entry.getKey(), roundScore(entry.getValue())))
                .toList();
    }

    private Map<Long, Integer> computePopularity(List<Sale> sales) {
        Map<Long, Integer> popularity = new HashMap<>();
        for (Sale sale : sales) {
            for (SaleItem item : sale.getLines()) {
                Long productId = item.getProduct().getId();
                int qty = item.getQuantity() == null ? 0 : item.getQuantity();
                popularity.merge(productId, qty, Integer::sum);
            }
        }
        return popularity;
    }

    private Map<Long, Integer> computeSupportCountByProduct(List<Sale> sales) {
        Map<Long, Integer> support = new HashMap<>();
        for (Sale sale : sales) {
            Set<Long> productsInTransaction = sale.getLines().stream()
                    .map(item -> item.getProduct().getId())
                    .collect(Collectors.toSet());
            for (Long productId : productsInTransaction) {
                support.merge(productId, 1, Integer::sum);
            }
        }
        return support;
    }

    private Map<Long, Map<Long, Integer>> computePairSupportCount(List<Sale> sales) {
        Map<Long, Map<Long, Integer>> matrix = new HashMap<>();

        for (Sale sale : sales) {
            Set<Long> productsInTransaction = sale.getLines().stream()
                    .map(item -> item.getProduct().getId())
                    .collect(Collectors.toCollection(HashSet::new));

            List<Long> ids = new ArrayList<>(productsInTransaction);
            for (int i = 0; i < ids.size(); i++) {
                for (int j = i + 1; j < ids.size(); j++) {
                    Long left = ids.get(i);
                    Long right = ids.get(j);

                    matrix.computeIfAbsent(left, k -> new HashMap<>()).merge(right, 1, Integer::sum);
                    matrix.computeIfAbsent(right, k -> new HashMap<>()).merge(left, 1, Integer::sum);
                }
            }
        }

        return matrix;
    }

    private List<RecommendationCandidate> bestSellers(List<Product> catalog, Map<Long, Integer> popularity, int limit) {
        if (popularity.isEmpty()) {
            return catalog.stream()
                    .sorted(Comparator.comparing(Product::getId).reversed())
                    .limit(limit)
                    .map(product -> new RecommendationCandidate(product.getId(), 0.05))
                    .toList();
        }

        Set<Long> available = catalog.stream().map(Product::getId).collect(Collectors.toSet());
        List<RecommendationCandidate> ranked = popularity.entrySet().stream()
                .filter(entry -> available.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> new RecommendationCandidate(entry.getKey(), roundScore(entry.getValue())))
                .toList();

        if (ranked.size() >= limit) {
            return ranked;
        }

        List<Long> rankedIds = ranked.stream().map(RecommendationCandidate::productId).toList();
        List<RecommendationCandidate> fallback = catalog.stream()
                .filter(product -> !rankedIds.contains(product.getId()))
                .sorted(Comparator.comparing(Product::getId).reversed())
                .limit(limit - ranked.size())
                .map(product -> new RecommendationCandidate(product.getId(), 0.05))
                .toList();

        List<RecommendationCandidate> merged = new ArrayList<>(ranked);
        merged.addAll(fallback);
        return merged;
    }

    private List<RecommendationCandidate> bestSellersExcluding(List<Product> catalog, Map<Long, Integer> popularity, int limit,
            Long excludedProductId) {
        return bestSellers(catalog, popularity, limit + 1).stream()
                .filter(candidate -> !candidate.productId().equals(excludedProductId))
                .limit(limit)
                .toList();
    }

    private double roundScore(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
