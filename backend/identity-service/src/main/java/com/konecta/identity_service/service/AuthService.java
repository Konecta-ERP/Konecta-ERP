package com.konecta.identity_service.service;

import com.konecta.identity_service.dto.ApiResponse;
import com.konecta.identity_service.dto.LoginRequest;
import com.konecta.identity_service.dto.LoginResponse;

import java.util.Map;

public interface AuthService {
    ApiResponse<LoginResponse> getToken(LoginRequest request);
    Map<String, Object> getPublicKey();
}
