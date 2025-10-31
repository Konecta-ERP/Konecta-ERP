package com.konecta.identity_service.service;

import com.konecta.identity_service.dto.request.ChangePasswordRequest;
import com.konecta.identity_service.dto.request.CreateUserRequest;
import com.konecta.identity_service.dto.request.UpdateUserRequest;
import com.konecta.identity_service.dto.response.UserResponse;
import com.konecta.identity_service.entity.Role;
import com.konecta.identity_service.entity.User;
import com.konecta.identity_service.exception.DuplicateResourceException;
import com.konecta.identity_service.exception.InvalidRequestException;
import com.konecta.identity_service.exception.ResourceNotFoundException;
import com.konecta.identity_service.mapper.UserMapper;
import com.konecta.identity_service.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
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

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Duplicate entry for email " + request.getEmail(),
                    "Email already exists."
            );
        }
        if (request.getRole() == null || !request.getRole().isAssignable()) {
            throw new InvalidRequestException(
                    "Role " + request.getRole() + " is not assignable.",
                    "A valid, assignable role must be provided."
            );
        }

        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user exists with ID " + id,
                        "User not found."
                ));
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user exists with ID " + id, "User not found."
                ));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new DuplicateResourceException(
                        "Email " + request.getEmail() + " is already in use.",
                        "Email already exists."
                );
            }
        }
        userMapper.updateUserFromDto(request, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("No user exists with ID " + id, "User not found.");
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<String> getAllRoles() {
        return Arrays.stream(Role.values())
                .filter(Role::isAssignable)
                .map(Enum::name)
                .toList();
    }

    @Override
    public UserResponse assignRoleToUser(UUID id, Role role) {
        if (!role.isAssignable()) {
            throw new InvalidRequestException(
                    "Role " + role.name() + " is not an assignable role.",
                    "Invalid role specified."
            );
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user exists with ID " + id, "User not found."));

        user.setRole(role);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserResponse revokeRoleFromUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user exists with ID " + id, "User not found."));

        if (user.getRole() == null) {
            throw new InvalidRequestException(
                    "User does not have a high-level role to revoke.",
                    "User is already a base employee."
            );
        }
        user.setRole(null);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserResponse activateUserById(UUID id) {
        return setUserActiveStatus(id, true);
    }

    @Override
    public UserResponse deactivateUserById(UUID id) {
        return setUserActiveStatus(id, false);
    }

    @Override
    public void updatePassword(UUID id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user exists with ID " + id, "User not found."));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Old password does not match.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserResponse setUserActiveStatus(UUID id, boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No user exists with ID " + id, "User not found."));

        user.setActive(isActive);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }
}