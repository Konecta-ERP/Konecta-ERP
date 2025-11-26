package com.konecta.identity_service.dto.request;

import com.konecta.identity_service.entity.Role;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    @NotNull(message = "A role must be specified")
    private Role role;
}