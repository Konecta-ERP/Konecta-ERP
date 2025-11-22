package com.konecta.identity_service.service;

import com.konecta.identity_service.dto.request.LoginRequest;
import com.konecta.identity_service.dto.response.LoginResponse;


import java.util.Map;

public interface AuthService {
    LoginResponse getToken(LoginRequest request);
    Map<String, Object> getPublicKey();
}