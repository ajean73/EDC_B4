package com.shopwise.app.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.entity.SaleItem;

class CoOccurrenceRecommendationStrategyTest {

    private final CoOccurrenceRecommendationStrategy strategy = new CoOccurrenceRecommendationStrategy();

    @Test
    void shouldRecommendAssociatedProductUsingSupportConfidenceAndLift() {
        Product p1 = product(1L, "Laptop", "1000.00");
        Product p2 = product(2L, "Souris", "50.00");
        Product p3 = product(3L, "Clavier", "80.00");

        Sale s1 = saleOf(line(p1, 1), line(p2, 1));
        Sale s2 = saleOf(line(p1, 1), line(p2, 1));
        Sale s3 = saleOf(line(p1, 1), line(p2, 1), line(p3, 1));
        Sale s4 = saleOf(line(p3, 1));

        List<RecommendationCandidate> result = strategy.recommend(
                List.of(s1, s2, s3, s4),
                List.of(p1, p2, p3),
                1L,
                5);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).productId()).isEqualTo(2L);
        assertThat(result.get(0).score()).isGreaterThan(0.0);
    }

    @Test
    void shouldFallbackToBestSellersWhenReferenceProductIsMissingInHistory() {
        Product p1 = product(1L, "Laptop", "1000.00");
        Product p2 = product(2L, "Souris", "50.00");
        Product p3 = product(3L, "Clavier", "80.00");

        Sale s1 = saleOf(line(p2, 3), line(p3, 1));

        List<RecommendationCandidate> result = strategy.recommend(
                List.of(s1),
                List.of(p1, p2, p3),
                1L,
                2);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RecommendationCandidate::productId)
                .doesNotContain(1L);
    }

    @Test
    void shouldReturnBestSellersWhenReferenceProductIsNull() {
        Product p1 = product(1L, "Laptop", "1000.00");
        Product p2 = product(2L, "Souris", "50.00");

        Sale s1 = saleOf(line(p2, 2));

        List<RecommendationCandidate> result = strategy.recommend(
                List.of(s1),
                List.of(p1, p2),
                null,
                5);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).productId()).isEqualTo(2L);
    }

    private Product product(Long id, String name, String price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        return product;
    }

    private Sale saleOf(SaleItem... items) {
        Sale sale = new Sale();
        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem item : items) {
            sale.addLine(item);
            total = total.add(item.getItemTotal());
        }
        sale.setTotalAmount(total);
        return sale;
    }

    private SaleItem line(Product product, int quantity) {
        SaleItem item = new SaleItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        item.setItemTotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return item;
    }
}
