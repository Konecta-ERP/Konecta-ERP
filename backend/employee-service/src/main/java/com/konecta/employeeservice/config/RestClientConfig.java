package com.konecta.employeeservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @LoadBalanced // Applies Load Balancing to this RestClient Builder
    public RestClient.Builder loadBalancedRestClientBuilder() {
        // You can add default settings here if needed, but not required for load balancing.
        return RestClient.builder();
    }
}