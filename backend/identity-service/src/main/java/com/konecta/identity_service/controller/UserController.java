package com.konecta.identity_service.controller;

import com.konecta.identity_service.dto.request.*;
import com.konecta.identity_service.dto.response.ApiResponse;
import com.konecta.identity_service.dto.response.UserResponse;
import com.konecta.identity_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/identity")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    @PreAuthorize("hasAuthority('HR_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse user = userService.createUser(request);
        ApiResponse<UserResponse> response = ApiResponse.success(
                user, 201, "User registered successfully.", "New user created");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {

        List<UserResponse> users = userService.getAllUsers();
        ApiResponse<List<UserResponse>> response = ApiResponse.success(
                users, 200, "User list retrieved successfully.", users.size() + " users found.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'HR_MANAGER') or #id.toString() == authentication.token.claims['userId']")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable("id") UUID id) {

        UserResponse user = userService.getUserById(id);
        ApiResponse<UserResponse> response = ApiResponse.success(
                user, 200, "User data retrieved.", "User found.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('HR_ADMIN') or #id.toString() == authentication.token.claims['userId']")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse user = userService.updateUser(id, request);
        ApiResponse<UserResponse> response = ApiResponse.success(
                user, 200, "User updated successfully.", "User record updated.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('HR_ADMIN')")
    public ResponseEntity<ApiResponse<?>> deleteUser(@PathVariable UUID id) {

        userService.deleteUser(id);
        ApiResponse<?> response = ApiResponse.success(
                204, "User deleted successfully.", "User record deleted.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('EMP')")
    public ResponseEntity<ApiResponse<List<String>>> getAllRoles() {

        List<String> roles = userService.getAllRoles();
        ApiResponse<List<String>> response = ApiResponse.success(
                roles, 200, "Roles retrieved successfully.", roles.size() + " assignable roles found.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/users/{id}/roles")
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoleToUser(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRoleRequest request) {

        UserResponse user = userService.assignRoleToUser(id, request.getRole());
        ApiResponse<UserResponse> response = ApiResponse.success(
                user, 200, "Role assigned successfully.", "User role updated.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/users/{id}/roles/role")
    @PreAuthorize("hasAnyAuthority('HR_ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserResponse>> revokeRoleFromUser(@PathVariable UUID id) {

        UserResponse user = userService.revokeRoleFromUser(id);
        ApiResponse<UserResponse> response = ApiResponse.success(
                user, 200, "Role revoked successfully.", "User demoted to base role.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/users/{id}/activate")
    @PreAuthorize("hasAuthority('HR_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable UUID id) {
        UserResponse user = userService.activateUserById(id);
        ApiResponse<UserResponse> response = ApiResponse.success(
                user, 200, "User activated.", "User status updated.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/users/{id}/deactivate")
    @PreAuthorize("hasAuthority('HR_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable UUID id) {
        UserResponse user = userService.deactivateUserById(id);
        ApiResponse<UserResponse> response = ApiResponse.success(
                user, 200, "User deactivated.", "User status updated.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/users/{id}/password")
    @PreAuthorize("#id.toString() == authentication.token.claims['userId']")
    public ResponseEntity<ApiResponse<?>> updatePassword(
            @PathVariable("id") UUID id,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.updatePassword(id, request);
        ApiResponse<?> response = ApiResponse.success(
                200, "Password updated successfully.", "Password changed.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody @Valid ForgetPasswordRequest request) {
        userService.generateOtp(request);
        return ResponseEntity.ok(ApiResponse.success(null, 200,
                "OTP sent via email",
                "OTP sent to " + request.getEmail()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyOtp(@RequestBody @Valid VerifyOtpRequest request) {
        String resetToken = userService.getPasswordResetToken(request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("resetToken",resetToken), 200,
                "OTP Verified",
                "Token for reset provided"));
    }

    @PostMapping("/reset-password")
    @PreAuthorize("hasAuthority('SCOPE_PWD_RESET')")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @RequestBody @Valid ResetPasswordRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        String email = jwt.getSubject();
        userService.resetPassword(email, request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success(null, 200, "Password successfully reset", null));
    }
}