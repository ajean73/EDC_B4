package com.shopwise.app.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import com.shopwise.app.config.SecurityConfig;
import com.shopwise.app.exception.CustomExceptionHandler;
import com.shopwise.app.exception.NotFoundException;
import com.shopwise.app.security.JwtRoleConverter;
import com.shopwise.app.security.RestAccessDeniedHandler;
import com.shopwise.app.security.RestAuthenticationEntryPoint;
import com.shopwise.app.service.ProductService;
import com.shopwise.app.service.SaleService;

@WebMvcTest(controllers = { ProductController.class, SaleController.class })
@Import({ SecurityConfig.class, JwtRoleConverter.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, CustomExceptionHandler.class })
class SecurityAndErrorHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private SaleService saleService;

        @MockBean
        private JwtDecoder jwtDecoder;

    @Test
    void shouldReturn401WhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));
    }

    @Test
        void shouldReturn403WhenUserRoleCallsAdminEndpoint() throws Exception {
        String payload = """
                {
                  "name": "Test",
                  "description": "Desc",
                  "price": 10.00
                }
                """;

        mockMvc.perform(post("/api/products")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    @Test
    void shouldReturn404WithStandardFormat() throws Exception {
        when(productService.getById(999L)).thenThrow(new NotFoundException("Product not found"));

        mockMvc.perform(get("/api/products/999")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void shouldReturn400WithStandardFormat() throws Exception {
        String invalidPayload = """
                {
                  "name": "",
                  "price": -1
                }
                """;

        mockMvc.perform(post("/api/products")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").exists());
    }
}
