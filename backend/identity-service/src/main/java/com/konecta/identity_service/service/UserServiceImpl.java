package com.konecta.identity_service.service;

import com.konecta.identity_service.dto.*;
import com.konecta.identity_service.entity.Role;
import com.konecta.identity_service.entity.User;
import com.konecta.identity_service.mapper.UserMapper;
import com.konecta.identity_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public ApiResponse<UserResponse> createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error(
                    409,
                    "Duplicate entry for email " + request.getEmail(),
                    "Email already exists."
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
    @Override
    public ApiResponse<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();

        return ApiResponse.success(
                users,
                200,
                "User list retrieved successfully.",
                users.size() + " users fetched from database"
        );
    }

    @Override
    public ApiResponse<UserResponse> getUserById(UUID id) {
        return userRepository.findById(id)
                .map(user -> ApiResponse.success(
                        userMapper.toUserResponse(user),
                        200,
                        "User data retrieved successfully.",
                        "User record found for ID " + id
                ))
                .orElse(ApiResponse.error(
                        404,
                        "No user exists with ID " + id,
                        "User not found."
                ));
    }

    @Override
    public ApiResponse<UserResponse> updateUser(UUID id, UpdateUserRequest request) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ApiResponse.error(404, "No user exists with ID " + id, "User not found.");
        }
        User user = userOpt.get();

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ApiResponse.error(
                        409,
                        "Email " + request.getEmail() + " is already in use.",
                        "Username or email already exists."
                );
            }
        }
        userMapper.updateUserFromDto(request, user);
        User updatedUser = userRepository.save(user);
        return ApiResponse.success(
                userMapper.toUserResponse(updatedUser),
                200,
                "User data updated successfully.",
                "User record updated for ID " + id
        );
    }

    @Override
    public ApiResponse<?> deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            return ApiResponse.error(404, "No user exists with ID " + id, "User not found.");
        }
        userRepository.deleteById(id);
        return ApiResponse.success(
                204,
                "User deleted successfully.",
                "User record deleted for ID " + id
        );
    }

    @Override
    public ApiResponse<?> getAllRoles() {
        List<String> roles = Arrays.stream(Role.values())
                .map(Enum::name)
                .toList();
        return ApiResponse.success(
                roles,
                200,
                "Roles retrieved successfully.",
                roles.size() + " roles found."
        );
    }

    @Override
    public ApiResponse<UserResponse> assignRoleToUser(UUID id, Role role) {
        if (!role.isAssignable()) {
            return ApiResponse.error(
                    400,
                    "Role " + role.name() + " is not an assignable role.",
                    "Invalid role specified."
            );
        }
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ApiResponse.error(404, "No user exists with ID " + id, "User not found.");
        }
        User user = userOpt.get();
        user.setRole(role);
        userRepository.save(user);

        return ApiResponse.success(
                userMapper.toUserResponse(user),
                200,
                "Role assigned successfully.",
                "Role '" + role + "' added to user ID " + id
        );
    }

    @Override
    public ApiResponse<UserResponse> revokeRoleFromUser(UUID id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ApiResponse.error(404, "No user exists with ID " + id, "User not found.");
        }
        User user = userOpt.get();

        if (user.getRole() == null) {
            return ApiResponse.error(
                    400,
                    "User does not have a high-level role to revoke.",
                    "User is already a base employee."
            );
        }
        Role role = user.getRole();
        user.setRole(null);
        userRepository.save(user);

        return ApiResponse.success(
                userMapper.toUserResponse(user),
                200,
                "Role removed successfully.",
                "Role " + role + " removed from user ID " + id
        );
    }

    @Override
    public ApiResponse<UserResponse> activateUserById(UUID id) {
        return setUserActiveStatus(id, true);
    }

    @Override
    public ApiResponse<UserResponse> deactivateUserById(UUID id) {
        return setUserActiveStatus(id, false);
    }

    @Override
    public ApiResponse<?> updatePassword(UUID id, ChangePasswordRequest request) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ApiResponse.error(404, "No user exists with ID " + id, "User not found.");
        }
        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            return ApiResponse.error(
                    400,
                    "Old password does not match.",
                    "Invalid credentials."
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.success(
                200,
                "Password updated successfully.",
                "Password changed for user " + user.getEmail()
        );
    }

    private ApiResponse<UserResponse> setUserActiveStatus(UUID id, boolean isActive) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ApiResponse.error(404, "No user exists with ID " + id, "User not found.");
        }

        User user = userOpt.get();
        user.setActive(isActive);
        User updatedUser = userRepository.save(user);
        String action = isActive ? "activated" : "deactivated";

        return ApiResponse.success(
                userMapper.toUserResponse(updatedUser),
                200,
                "User " + action + " successfully.",
                "User record " + action + " for ID " + id
        );
    }
}
