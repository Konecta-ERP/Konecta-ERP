package com.konecta.identity_service.controller;

import com.konecta.identity_service.dto.*;
import com.konecta.identity_service.entity.Role;
import com.konecta.identity_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/identity")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint: Create User
     * POST /api/identity/users
     */
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('HR_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        ApiResponse<UserResponse> response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint: Get All Users
     * GET /api/identity/users
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        ApiResponse<List<UserResponse>> response = userService.getAllUsers();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Endpoint: Get User by ID
     * GET /api/identity/users/{id}
     * Allows HR_ADMIN, HR_MANAGER, or the user themselves to view the profile.
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'HR_MANAGER') or #id.toString() == authentication.token.claims['userId']")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        ApiResponse<UserResponse> response = userService.getUserById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Endpoint: Update User by ID
     * PUT /api/identity/users/{id}
     * Allows HR_ADMIN or the user themselves to update.
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('HR_ADMIN') or #id.toString() == authentication.token.claims['userId']")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {

        ApiResponse<UserResponse> response = userService.updateUser(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Endpoint: Delete User by ID
     * DELETE /api/identity/users/{id}
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('HR_ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable UUID id) {
        ApiResponse<?> response = userService.deleteUser(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Endpoint: Get All Roles
     * GET /api/identity/roles
     */
    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()") // Any authenticated user can see the available roles
    public ResponseEntity<ApiResponse<?>> getAllRoles() {
        ApiResponse<?> response = userService.getAllRoles();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Endpoint: Assign Role to User
     * POST /api/identity/users/{id}/roles
     */
    @PostMapping("/users/{id}/roles")
    @PreAuthorize("hasAuthority('HR_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoleToUser(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRoleRequest request) {

        ApiResponse<UserResponse> response = userService.assignRoleToUser(id, request.getRole());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Endpoint: Revoke Role from User
     * DELETE /api/identity/users/{id}/roles/{role}
     */
    @DeleteMapping("/users/{id}/roles/{role}")
    @PreAuthorize("hasAuthority('HR_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> revokeRoleFromUser(
            @PathVariable UUID id,
            @PathVariable Role role) {

        ApiResponse<UserResponse> response = userService.revokeRoleFromUser(id, role);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Endpoint: Activate User
     * POST /api/identity/users/{id}/activate
     */
    @PostMapping("/users/{id}/activate")
    @PreAuthorize("hasAuthority('HR_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable UUID id) {
        ApiResponse<UserResponse> response = userService.activateUserById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Endpoint: Deactivate User
     * POST /api/identity/users/{id}/deactivate
     */
    @PostMapping("/users/{id}/deactivate")
    @PreAuthorize("hasAuthority('HR_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable UUID id) {
        ApiResponse<UserResponse> response = userService.deactivateUserById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Endpoint: Update User's Own Password
     * PUT /api/identity/users/{id}/password
     * Only the user themselves can change their password.
     */
    @PutMapping("/users/{id}/password")
    @PreAuthorize("#id.toString() == authentication.token.claims['userId']")
    public ResponseEntity<ApiResponse<?>> updatePassword(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request) {

        ApiResponse<?> response = userService.updatePassword(id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}