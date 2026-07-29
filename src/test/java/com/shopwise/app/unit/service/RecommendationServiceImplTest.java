package com.shopwise.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopwise.app.dto.response.RecommendationResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.recommendation.RecommendationCandidate;
import com.shopwise.app.recommendation.RecommendationStrategy;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.repository.SaleRepository;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private RecommendationStrategy recommendationStrategy;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @Test
    void shouldReturnMappedRecommendations() {
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Laptop");

        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Souris");

        List<Sale> sales = List.of(new Sale());
        List<Product> catalog = List.of(p1, p2);

        when(productRepository.existsById(1L)).thenReturn(true);
        when(saleRepository.findAllByOrderBySoldAtDesc()).thenReturn(sales);
        when(productRepository.findAll()).thenReturn(catalog);
        when(recommendationStrategy.recommend(sales, catalog, 1L, 5))
                .thenReturn(List.of(new RecommendationCandidate(2L, 1.234)));

        List<RecommendationResponse> result = recommendationService.recommend(1L, 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(2L);
        assertThat(result.get(0).getProductName()).isEqualTo("Souris");
        assertThat(result.get(0).getScore()).isEqualTo(1.234);
    }

    @Test
    void shouldThrowWhenReferenceProductDoesNotExist() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> recommendationService.recommend(99L, 5))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }
}
