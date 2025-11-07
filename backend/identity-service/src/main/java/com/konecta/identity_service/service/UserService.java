package com.konecta.identity_service.service;

import com.konecta.identity_service.dto.request.ChangePasswordRequest;
import com.konecta.identity_service.dto.request.CreateUserRequest;
import com.konecta.identity_service.dto.request.UpdateUserRequest;
import com.konecta.identity_service.dto.response.UserResponse;
import com.konecta.identity_service.entity.Role;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(UUID id);
    UserResponse updateUser(UUID id, UpdateUserRequest request);
    void deleteUser(UUID id);
    List<String> getAllRoles();
    UserResponse assignRoleToUser(UUID id, Role role);
    UserResponse revokeRoleFromUser(UUID id);
    UserResponse activateUserById(UUID id);
    UserResponse deactivateUserById(UUID id);
    void updatePassword(UUID id, ChangePasswordRequest request);
}