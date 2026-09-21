package com.secureops.backend.dto;

import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long id;
    private String actor;
    private String action;
    private String resourceType;
    private Long resourceId;
    private LocalDateTime timestamp;
    private String details;

    public AuditLogResponse() {
    }

    public AuditLogResponse(
            Long id,
            String actor,
            String action,
            String resourceType,
            Long resourceId,
            LocalDateTime timestamp,
            String details) {

        this.id = id;
        this.actor = actor;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.timestamp = timestamp;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public String getActor() {
        return actor;
    }

    public String getAction() {
        return action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }
}