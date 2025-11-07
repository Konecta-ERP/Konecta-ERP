package com.konecta.api_gateway.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class CorsConfig {
    @Value("${cors.allowed-origin-patterns}")
    private String allowedOriginPatterns;

    @Value("${cors.allowed-methods}")
    private String[] allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        List<String> originPatterns = Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (originPatterns.isEmpty()) {
            originPatterns = List.of("*");
        }

        boolean hasWildcardOrigin = originPatterns.stream()
                .anyMatch(pattern -> "*".equals(pattern) || "*:*".equals(pattern));

        // Browsers reject wildcard origins when credentials are allowed,
        // so disable credentials if a wildcard pattern is configured.
        config.setAllowCredentials(!hasWildcardOrigin && allowCredentials);
        config.setAllowedOriginPatterns(originPatterns);
        List<String> headerList = Arrays.stream(allowedHeaders.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (headerList.isEmpty()) {
            headerList = List.of("*");
        }

        config.setAllowedHeaders(headerList);
        config.setAllowedMethods(Arrays.asList(allowedMethods));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
