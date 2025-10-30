package com.konecta.identity_service.dto;

import com.konecta.identity_service.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AssignRoleRequest {
    @NotNull(message = "Role cannot be null")
    private Role role;
}