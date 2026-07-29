package com.shopwise.app.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.entity.SaleItem;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
class SaleRepositoryIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("shopwise_repo_test")
            .withUsername("shopwise")
            .withPassword("shopwise");

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driverClassName", POSTGRES::getDriverClassName);
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Test
    void shouldPersistSaleAndLoadLinesWithEntityGraph() {
        Product product = productRepository.save(buildProduct("Clavier", "45.00"));

        Sale savedSale = saleRepository.saveAndFlush(buildSale(product, 2));

        Sale found = saleRepository.findWithLinesById(savedSale.getId()).orElseThrow();

        assertThat(found.getLines()).hasSize(1);
        assertThat(found.getLines().get(0).getProduct().getId()).isEqualTo(product.getId());
        assertThat(found.getLines().get(0).getItemTotal()).isEqualByComparingTo("90.00");
    }

    @Test
    void shouldReturnSalesOrderedBySoldAtDesc() throws InterruptedException {
        Product product = productRepository.save(buildProduct("Souris", "25.00"));

        saleRepository.saveAndFlush(buildSale(product, 1));
        Thread.sleep(10);
        saleRepository.saveAndFlush(buildSale(product, 3));

        List<Sale> sales = saleRepository.findAllByOrderBySoldAtDesc();

        assertThat(sales).hasSize(2);
        assertThat(sales.get(0).getSoldAt()).isAfterOrEqualTo(sales.get(1).getSoldAt());
    }

    private Product buildProduct(String name, String price) {
        Product product = new Product();
        product.setName(name);
        product.setDescription("desc");
        product.setPrice(new BigDecimal(price));
        return product;
    }

    private Sale buildSale(Product product, int quantity) {
        BigDecimal unitPrice = product.getPrice();
        BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        SaleItem item = new SaleItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setItemTotal(itemTotal);

        Sale sale = new Sale();
        sale.setTotalAmount(itemTotal);
        sale.addLine(item);
        return sale;
    }
}
