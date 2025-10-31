package com.konecta.identity_service.dto;

import com.konecta.identity_service.entity.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Boolean active;
    private Set<Role> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}