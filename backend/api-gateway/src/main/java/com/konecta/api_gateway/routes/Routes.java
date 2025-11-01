package com.konecta.api_gateway.routes;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class Routes {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    // This is the key - create a primary RestClient bean that's load-balanced
    @Bean
    @Primary
    public RestClient restClient(@LoadBalanced RestClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public RouterFunction<ServerResponse> exampleServiceRoute() {
        return route("service-example")
                .route(path("/api/example/**"), http("lb://service-example"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> identityServiceRoute() {
        return route("identity-service")
                .route(path("/api/identity/**"), http("lb://identity-service"))
                .build();
    }
}
