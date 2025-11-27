package com.konecta.identity_service.service;

import com.konecta.identity_service.dto.request.*;
import com.konecta.identity_service.dto.response.UserResponse;
import com.konecta.identity_service.entity.Role;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
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
    void generateOtp(ForgetPasswordRequest request );
    String getPasswordResetToken(VerifyOtpRequest request);
    void resetPassword(String email, String newPassword);
    String createSeedUser(Map<String, String> request);
}