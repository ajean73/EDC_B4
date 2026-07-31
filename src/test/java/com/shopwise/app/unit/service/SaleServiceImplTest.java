package com.shopwise.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopwise.app.dto.request.CreateSaleItemRequest;
import com.shopwise.app.dto.request.CreateSaleRequest;
import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.dto.response.SaleSummaryResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.entity.Sale;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.mapper.SaleMapper;
import com.shopwise.app.repository.ProductRepository;
import com.shopwise.app.repository.SaleRepository;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SaleMapper saleMapper;

    @InjectMocks
    private SaleServiceImpl saleService;

    @Test
    void shouldCreateSaleAndComputeTotal() {
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Laptop");
        p1.setPrice(new BigDecimal("1000.00"));

        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Souris");
        p2.setPrice(new BigDecimal("50.00"));

        CreateSaleItemRequest line1 = new CreateSaleItemRequest();
        line1.setProductId(1L);
        line1.setQuantity(1);

        CreateSaleItemRequest line2 = new CreateSaleItemRequest();
        line2.setProductId(2L);
        line2.setQuantity(2);

        CreateSaleRequest request = new CreateSaleRequest();
        request.setLines(List.of(line1, line2));

        when(productRepository.findAllById(any())).thenReturn(List.of(p1, p2));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            sale.setId(10L);
            return sale;
        });

        SaleResponse mapped = new SaleResponse();
        mapped.setId(10L);
        mapped.setTotalAmount(new BigDecimal("1100.00"));
        when(saleMapper.toResponse(any(Sale.class))).thenReturn(mapped);

        SaleResponse result = saleService.create(request);

        ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
        verify(saleRepository).save(saleCaptor.capture());

        Sale persisted = saleCaptor.getValue();
        assertThat(persisted.getLines()).hasSize(2);
        assertThat(persisted.getTotalAmount()).isEqualByComparingTo("1100.00");
        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void shouldThrowWhenOneProductDoesNotExist() {
        Product onlyProduct = new Product();
        onlyProduct.setId(1L);
        onlyProduct.setPrice(new BigDecimal("10.00"));

        CreateSaleItemRequest line1 = new CreateSaleItemRequest();
        line1.setProductId(1L);
        line1.setQuantity(1);

        CreateSaleItemRequest line2 = new CreateSaleItemRequest();
        line2.setProductId(99L);
        line2.setQuantity(1);

        CreateSaleRequest request = new CreateSaleRequest();
        request.setLines(List.of(line1, line2));

        when(productRepository.findAllById(any())).thenReturn(List.of(onlyProduct));

        assertThatThrownBy(() -> saleService.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");
    }

    @Test
    void shouldReturnSummariesOnGetAll() {
        Sale sale = new Sale();
        sale.setId(3L);

        SaleSummaryResponse summary = new SaleSummaryResponse();
        summary.setId(3L);

        when(saleRepository.findAllByOrderBySoldAtDesc()).thenReturn(List.of(sale));
        when(saleMapper.toSummaryList(List.of(sale))).thenReturn(List.of(summary));

        List<SaleSummaryResponse> result = saleService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(3L);
    }

    @Test
    void shouldThrowWhenSaleNotFoundOnGetById() {
        when(saleRepository.findWithLinesById(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.getById(50L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Sale not found");
    }
}
