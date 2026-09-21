package com.secureops.backend.dto;

import com.secureops.backend.entity.AccountStatus;
import com.secureops.backend.entity.Role;

public class UserResponse {

    private Long id;
    private String email;
    private Role role;
    private AccountStatus status;

    public UserResponse() {
    }

    public UserResponse(
            Long id,
            String email,
            Role role,
            AccountStatus status) {

        this.id = id;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getStatus() {
        return status;
    }
}