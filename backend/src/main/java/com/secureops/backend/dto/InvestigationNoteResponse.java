package com.secureops.backend.dto;

public class InvestigationNoteResponse {

    private Long id;
    private String content;
    private String createdByEmail;

    public InvestigationNoteResponse() {
    }

    public InvestigationNoteResponse(
            Long id,
            String content,
            String createdByEmail) {

        this.id = id;
        this.content = content;
        this.createdByEmail = createdByEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }
}