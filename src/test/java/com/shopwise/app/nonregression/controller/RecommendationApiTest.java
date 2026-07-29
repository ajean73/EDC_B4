package com.shopwise.app.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.shopwise.app.config.SecurityConfig;
import com.shopwise.app.dto.response.RecommendationResponse;
import com.shopwise.app.exception.CustomExceptionHandler;
import com.shopwise.app.security.JwtRoleConverter;
import com.shopwise.app.security.RestAccessDeniedHandler;
import com.shopwise.app.security.RestAuthenticationEntryPoint;
import com.shopwise.app.service.ProductService;
import com.shopwise.app.service.RecommendationService;
import com.shopwise.app.service.SaleService;

@WebMvcTest(controllers = { RecommendationController.class })
@Import({ SecurityConfig.class, JwtRoleConverter.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, CustomExceptionHandler.class })
class RecommendationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private SaleService saleService;

    @Test
    void shouldReturnRecommendationsForUserRole() throws Exception {
        RecommendationResponse response = new RecommendationResponse();
        response.setProductId(2L);
        response.setProductName("Souris");
        response.setScore(1.321);

        when(recommendationService.recommend(eq(1L), anyInt())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/recommendations?productId=1&limit=3")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(2))
                .andExpect(jsonPath("$[0].productName").value("Souris"));
    }

    @Test
    void shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/recommendations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }
}
