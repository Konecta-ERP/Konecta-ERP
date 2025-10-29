package com.konecta.identity_service.service;

import com.konecta.identity_service.dto.ApiResponse;
import com.konecta.identity_service.dto.LoginRequest;
import com.konecta.identity_service.dto.LoginResponse;
import com.konecta.identity_service.entity.User;
import com.konecta.identity_service.mapper.UserMapper;
import com.konecta.identity_service.repository.UserRepository;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JWKSet jwkSet;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthService(JwtService jwtService, AuthenticationManager authenticationManager, JWKSet jwkSet, UserRepository userRepository, UserMapper userMapper) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.jwkSet = jwkSet;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public ApiResponse<LoginResponse> getToken(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtService.generateToken(authentication);
        LoginResponse response = new LoginResponse(userMapper.toDto(user), token);
        return ApiResponse.success(response, 200,
                "User is successfully logged in.",
                "Access token is generated for 24 hours");
    }

    public Map<String, Object> getPublicKey() {
        return this.jwkSet.toJSONObject();
    }
}
