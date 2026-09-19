package com.secureops.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AssignIncidentRequest {

    @NotBlank(message = "Analyst email is required")
    @Email(message = "Analyst email must be valid")
    private String analystEmail;

    public AssignIncidentRequest() {
    }

    public String getAnalystEmail() {
        return analystEmail;
    }

    public void setAnalystEmail(String analystEmail) {
        this.analystEmail = analystEmail;
    }
}