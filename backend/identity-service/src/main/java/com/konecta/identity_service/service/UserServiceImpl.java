package com.konecta.identity_service.service;

import com.konecta.identity_service.dto.ApiResponse;
import com.konecta.identity_service.dto.CreateUserRequest;
import com.konecta.identity_service.dto.UserResponse;
import com.konecta.identity_service.entity.User;
import com.konecta.identity_service.mapper.UserMapper;
import com.konecta.identity_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl  {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public ApiResponse<UserResponse> createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ApiResponse.error(
                    409,
                    "Duplicate entry for email " + request.getEmail(),
                    "Username or email already exists."
            );
        }
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        User savedUser = userRepository.save(user);
        return ApiResponse.success(
                userMapper.toUserResponse(savedUser),
                201,
                "User registered successfully.",
                "New user created with ID " + user.getId()
        );
    }
}
