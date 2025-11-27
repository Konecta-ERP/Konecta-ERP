package com.konecta.employeeservice.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class IdentityClient {

    @LoadBalanced
    private final RestClient restClient;

    public IdentityClient(RestClient.Builder loadBalancedRestClientBuilder) {
        // Ensure this matches your actual service name (identity-service)
        // as registered in Eureka/Consul or Docker network DNS.
        String identityUrl = "http://identity-service/api/identity";

        this.restClient = loadBalancedRestClientBuilder
                .baseUrl(identityUrl)
                .build();
    }

    /**
     * Sends request to POST /users/seed
     * Expects a plain String UUID in response.
     */
    public UUID createUser(Map<String, Object> userRequest) {
        try {
            // 1. Call the endpoint
            String responseId = restClient.post()
                    .uri("/users/seed") // Updated Endpoint
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(userRequest)
                    .retrieve()
                    .body(String.class);

            // 2. Parse the String to UUID
            if (responseId != null && !responseId.isBlank()) {
                // Remove quotes if the JSON serializer added them (e.g. "uuid")
                String cleanId = responseId.replace("\"", "");
                System.out.println("DEBUG: Attempting to parse UUID: [" + cleanId + "] Length: " + cleanId.length());
                return UUID.fromString(cleanId);
            }
            return null;

        } catch (Exception e) {
            System.err.println("Failed to create user in Identity Service: " + e.getMessage());
            return null;
        }
    }

    public void assignUserRole(UUID userId, String role, String token) {
        try {
            restClient.patch()
                    .uri("/users/{id}/roles", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("role", role))
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign role '" + role + "' to user " + userId, e);
        }
    }
}