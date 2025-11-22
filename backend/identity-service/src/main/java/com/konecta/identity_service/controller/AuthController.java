package com.konecta.identity_service.controller;

import com.konecta.identity_service.dto.response.ApiResponse;
import com.konecta.identity_service.dto.request.LoginRequest;
import com.konecta.identity_service.dto.response.LoginResponse;
import com.konecta.identity_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/identity/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse loginResponse = authService.getToken(request);
        ApiResponse<LoginResponse> response = ApiResponse.success(
                loginResponse, 200,
                "User is successfully logged in.",
                "Access token generated for " + loginResponse.getUser().getEmail()
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        Map<String, Object> response = authService.getPublicKey();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}