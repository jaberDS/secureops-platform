package com.secureops.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateInvestigationNoteRequest {

    @NotBlank(message = "Note content is required")
    @Size(
            max = 5000,
            message = "Note content must not exceed 5000 characters"
    )
    private String content;

    public CreateInvestigationNoteRequest() {
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}