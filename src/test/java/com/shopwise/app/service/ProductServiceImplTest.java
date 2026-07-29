package com.shopwise.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopwise.app.dto.request.CreateProductRequest;
import com.shopwise.app.dto.request.UpdateProductRequest;
import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.entity.Product;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.mapper.ProductMapper;
import com.shopwise.app.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void shouldCreateProduct() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Clavier");
        request.setDescription("Mecanique");
        request.setPrice(new BigDecimal("79.90"));

        Product entity = new Product();
        entity.setName("Clavier");

        Product saved = new Product();
        saved.setId(1L);
        saved.setName("Clavier");

        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("Clavier");

        when(productMapper.toEntity(request)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(response);

        ProductResponse result = productService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Clavier");
    }

    @Test
    void shouldUpdateExistingProduct() {
        Product existing = new Product();
        existing.setId(5L);
        existing.setName("Ancien");

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Nouveau");
        request.setDescription("Desc");
        request.setPrice(new BigDecimal("15.00"));

        ProductResponse response = new ProductResponse();
        response.setId(5L);
        response.setName("Nouveau");

        when(productRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);
        when(productMapper.toResponse(existing)).thenReturn(response);

        ProductResponse result = productService.update(5L, request);

        verify(productMapper).updateEntity(request, existing);
        assertThat(result.getName()).isEqualTo("Nouveau");
    }

    @Test
    void shouldThrowWhenProductNotFoundOnGetById() {
        when(productRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(42L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    void shouldThrowWhenProductNotFoundOnDelete() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Product not found");
    }
}
