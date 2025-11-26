package com.konecta.identity_service.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Defines the roles within the system.
 * Roles are hierarchical and have an 'assignable' property.
 * - Assignable roles are "high-level" roles that can be set as a user's primary role.
 * - Non-assignable roles are "implied" roles used for authorization.
 */
@Getter
public enum Role {
    // Base Role (Not directly assignable, but can be used in authorization)
    EMP(true),
    MANAGER(true, EMP),

    // Department Roles (Not directly assignable, but can be used in authorization)
    HR_EMP(false, EMP),
    FINANCE_EMP(false, EMP),

    // High-Level Assignable Roles (HR)
    HR_ASSOCIATE(true, HR_EMP),
    HR_MANAGER(true, HR_EMP, MANAGER),
    HR_ADMIN(true, HR_EMP),

    // High-Level Assignable Roles (Finance)
    ACCOUNTANT(true, FINANCE_EMP),
    CFO(true, FINANCE_EMP, MANAGER);

    private final boolean assignable;
    private final Set<Role> impliedRoles;

    /**
     * Constructor for a role.
     * @param assignable true if this is a high-level role that can be assigned to a user.
     * @param implied The roles that this role automatically includes.
     */
    Role(boolean assignable, Role... implied) {
        this.assignable = assignable;
        this.impliedRoles = Stream.concat(
                Stream.of(this),
                Arrays.stream(implied)
                        .flatMap(r -> r.getImpliedRoles().stream())
        ).collect(Collectors.toSet());
    }

}