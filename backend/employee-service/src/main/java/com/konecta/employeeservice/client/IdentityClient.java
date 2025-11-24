package com.konecta.employeeservice.client;

import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
public class IdentityClient {

    private final RestClient restClient;

    public IdentityClient() {
        String identityUrl = "http://identity-service/api/identity";
        this.restClient = RestClient.builder()
                .baseUrl(identityUrl)
                .requestFactory(new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public void assignUserRole(UUID userId, String role, String token) {
        try {
            restClient.patch()
                    .uri("/users/{id}/roles", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("role", role))
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity(); // We just want 200 OK, ignore body
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign role '" + role + "' to user " + userId, e);
        }
    }
}