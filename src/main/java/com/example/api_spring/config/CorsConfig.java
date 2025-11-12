package com.example.api_spring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {


  @Value("${app.cors.allowed-origins:*}")
  private String allowedOrigins;

  @Bean
    public CorsFilter corsFilter() {
        var cfg = new org.springframework.web.cors.CorsConfiguration();
        cfg.setAllowedOriginPatterns(
            java.util.Arrays.stream(allowedOrigins.split(",")).map(String::trim).toList()
        );
        cfg.setAllowedMethods(java.util.List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(java.util.List.of("*"));
        cfg.setAllowCredentials(true);

        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new org.springframework.web.filter.CorsFilter(source);
    }
}
