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

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.stripPrefix;

import java.net.URI;

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
    public RouterFunction<ServerResponse> employeeServiceRoute() {
        return route("employee-service")
                .route(path("/api/employees/**"), http("lb://employee-service"))
                .route(path("/api/departments/**"), http("lb://employee-service"))
                .route(path("/api/attendance/**"), http("lb://employee-service"))
                .route(path("/api/leaves/**"), http("lb://employee-service"))
                .route(path("/leave-requests/**"), http("lb://employee-service"))
                .route(path("/api/offboarding/**"), http("lb://employee-service"))
                .route(path("/api/performance/**"), http("lb://employee-service"))
                .route(path("/api/leave-requests/**"), http("lb://employee-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> recruitmentServiceRoute() {
        return route("recruitment-service")
                .route(path("/api/job-posts/**"), http("lb://recruitment-service"))
                .route(path("/api/applicants/**"), http("lb://recruitment-service"))
                .route(path("/api/interviews/**"), http("lb://recruitment-service"))
                .route(path("/api/offers/**"), http("lb://recruitment-service"))
                .route(path("/api/requisitions/**"), http("lb://recruitment-service"))
                .route(path("/api/job-requisitions/**"), http("lb://recruitment-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> identityServiceRoute() {
        return route("identity-service")
                .route(path("/api/identity/**"), http("lb://identity-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> financeServiceRoute() {
        return route("finance-service")
                .route(path("/api/finance/**"), http("lb://finance-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> chatbotServiceRoute() {
        return route("ai-chatbot")
                .route(path("/api/chatbot/**"), http("lb://AI-CHATBOT"))
                .filter(stripPrefix(2))
                .build();
    }

}
