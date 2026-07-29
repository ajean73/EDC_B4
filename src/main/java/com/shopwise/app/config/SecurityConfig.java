package com.shopwise.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import com.shopwise.app.security.RestAccessDeniedHandler;
import com.shopwise.app.security.RestAuthenticationEntryPoint;
import com.shopwise.app.security.JwtRoleConverter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final JwtRoleConverter jwtRoleConverter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

        public SecurityConfig(JwtRoleConverter jwtRoleConverter,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) {
                this.jwtRoleConverter = jwtRoleConverter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/products", "/api/sales")
                        .hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/products/**")
                        .hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/products/**")
                        .hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products", "/api/products/**",
                                "/api/sales", "/api/sales/**", "/api/recommendations")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/products", "/api/products/**", "/api/sales", "/api/sales/**",
                                "/api/recommendations")
                        .authenticated()
                                                .anyRequest().permitAll())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
                JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
                converter.setJwtGrantedAuthoritiesConverter(jwtRoleConverter);
                return converter;
        }
}
