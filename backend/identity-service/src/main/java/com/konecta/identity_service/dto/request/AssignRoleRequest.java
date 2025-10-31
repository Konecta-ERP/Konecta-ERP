package com.konecta.identity_service.dto.request;

import com.konecta.identity_service.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignRoleRequest {
    @NotNull(message = "Role cannot be null")
    private Role role;
}