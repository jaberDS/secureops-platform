package com.secureops.backend.dto;

import com.secureops.backend.entity.Role;
import jakarta.validation.constraints.NotNull;

public class UpdateUserRoleRequest {

    @NotNull(message = "Role is required")
    private Role role;

    public UpdateUserRoleRequest() {
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}