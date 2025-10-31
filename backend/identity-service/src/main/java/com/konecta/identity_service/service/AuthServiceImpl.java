package com.konecta.identity_service.service;

import com.konecta.identity_service.dto.request.LoginRequest;
import com.konecta.identity_service.dto.response.LoginResponse;
import com.konecta.identity_service.dto.response.UserResponse;
import com.konecta.identity_service.entity.User;
import com.konecta.identity_service.mapper.UserMapper;
import com.nimbusds.jose.jwk.JWKSet;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JWKSet jwkSet;
    private final UserMapper userMapper;

    public AuthServiceImpl(JwtService jwtService, AuthenticationManager authenticationManager, JWKSet jwkSet, UserMapper userMapper) {
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.jwkSet = jwkSet;
        this.userMapper = userMapper;
    }
    @Override
    public LoginResponse getToken(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtService.generateToken(authentication);
        UserResponse user = userMapper.toUserResponse((User) authentication.getPrincipal());
        return new LoginResponse(user, token);
    }

    @Override
    public Map<String, Object> getPublicKey() {
        return this.jwkSet.toJSONObject();
    }
}
