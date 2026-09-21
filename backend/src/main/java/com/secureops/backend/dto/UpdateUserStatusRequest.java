package com.secureops.backend.dto;

import com.secureops.backend.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateUserStatusRequest {

    @NotNull(message = "Status is required")
    private AccountStatus status;

    public UpdateUserStatusRequest() {
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}