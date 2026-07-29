package com.shopwise.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.shopwise.app.config.SecurityConfig;
import com.shopwise.app.dto.response.ProductResponse;
import com.shopwise.app.dto.response.SaleResponse;
import com.shopwise.app.dto.response.SaleSummaryResponse;
import com.shopwise.app.exception.CustomExceptionHandler;
import com.shopwise.app.security.JwtRoleConverter;
import com.shopwise.app.security.RestAccessDeniedHandler;
import com.shopwise.app.security.RestAuthenticationEntryPoint;
import com.shopwise.app.service.ProductService;
import com.shopwise.app.service.SaleService;

@WebMvcTest(controllers = { ProductController.class, SaleController.class })
@Import({ SecurityConfig.class, JwtRoleConverter.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, CustomExceptionHandler.class })
class CatalogAndSalesApiTest {

    @Autowired
    private MockMvc mockMvc;

        @MockitoBean
    private ProductService productService;

        @MockitoBean
    private SaleService saleService;

        @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldListProductsForUserRole() throws Exception {
        ProductResponse product = new ProductResponse();
        product.setId(1L);
        product.setName("Ecran");
        product.setPrice(new BigDecimal("249.99"));

        when(productService.getAll()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Ecran"));
    }

    @Test
    void shouldCreateSaleForAdminRole() throws Exception {
        SaleResponse response = new SaleResponse();
        response.setId(100L);
        response.setTotalAmount(new BigDecimal("300.00"));

        when(saleService.create(any())).thenReturn(response);

        String payload = """
                {
                  "lines": [
                    { "productId": 1, "quantity": 2 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/sales")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.totalAmount").value(300.00));
    }

    @Test
    void shouldKeepHistoricalSaleReadEndpointForUserRole() throws Exception {
        SaleSummaryResponse summary = new SaleSummaryResponse();
        summary.setId(9L);
        summary.setLineCount(2);

        when(saleService.getAll()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/sales")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9))
                .andExpect(jsonPath("$[0].lineCount").value(2));
    }
}
