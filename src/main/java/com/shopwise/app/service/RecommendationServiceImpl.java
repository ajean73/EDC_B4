package com.shopwise.app.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.shopwise.app.dto.response.RecommendationResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.recommendation.RecommendationCandidate;
import com.shopwise.app.recommendation.RecommendationStrategy;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.repository.SaleRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RecommendationServiceImpl implements RecommendationService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final RecommendationStrategy recommendationStrategy;

    public RecommendationServiceImpl(SaleRepository saleRepository, ProductRepository productRepository,
            RecommendationStrategy recommendationStrategy) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.recommendationStrategy = recommendationStrategy;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<RecommendationResponse> recommend(Long productId, int limit) {
        if (productId != null && !productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found");
        }

        List<Sale> sales = saleRepository.findAllByOrderBySoldAtDesc();
        List<Product> catalog = productRepository.findAll();

        List<RecommendationCandidate> ranked = recommendationStrategy.recommend(sales, catalog, productId, limit);

        Map<Long, Product> productsById = catalog.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        return ranked.stream()
                .map(candidate -> toResponse(candidate, productsById))
                .filter(response -> response != null)
                .toList();
    }

    private RecommendationResponse toResponse(RecommendationCandidate candidate, Map<Long, Product> productsById) {
        Product product = productsById.get(candidate.productId());
        if (product == null) {
            return null;
        }

        RecommendationResponse response = new RecommendationResponse();
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setScore(candidate.score());
        return response;
    }
}
