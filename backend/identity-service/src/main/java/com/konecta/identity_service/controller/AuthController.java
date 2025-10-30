package com.konecta.identity_service.controller;

import com.konecta.identity_service.dto.ApiResponse;
import com.konecta.identity_service.dto.LoginRequest;
import com.konecta.identity_service.dto.LoginResponse;
import com.konecta.identity_service.service.AuthServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final AuthServiceImpl authService;

    public AuthController(AuthServiceImpl authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request) {
        ApiResponse<LoginResponse> response = authService.getToken(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        Map<String, Object> response = authService.getPublicKey();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
