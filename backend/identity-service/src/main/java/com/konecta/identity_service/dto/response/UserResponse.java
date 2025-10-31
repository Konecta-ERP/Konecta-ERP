package com.konecta.identity_service.dto.response;

import com.konecta.identity_service.entity.Role;
import lombok.*;

import java.time.LocalDateTime;
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
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}