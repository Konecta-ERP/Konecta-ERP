package com.konecta.identity_service.service;

import com.konecta.identity_service.dto.*;
import com.konecta.identity_service.entity.Role;

import java.util.List;
import java.util.UUID;

public interface UserService {
    ApiResponse<UserResponse> createUser(CreateUserRequest request);
    ApiResponse<List<UserResponse>> getAllUsers();
    ApiResponse<UserResponse> getUserById(UUID id);
    ApiResponse<UserResponse> updateUser(UUID id, UpdateUserRequest request);
    ApiResponse<?> deleteUser(UUID id);
    ApiResponse<?> getAllRoles();
    ApiResponse<UserResponse> assignRoleToUser(UUID id, Role role);
    ApiResponse<UserResponse> revokeRoleFromUser(UUID id, Role role);
    ApiResponse<UserResponse> activateUserById(UUID id);
    ApiResponse<UserResponse> deactivateUserById(UUID id);
    ApiResponse<?> updatePassword(UUID id, ChangePasswordRequest request);
}