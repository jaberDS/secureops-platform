package com.secureops.backend.dto;

import com.secureops.backend.entity.IncidentCategory;
import com.secureops.backend.entity.IncidentSeverity;
import com.secureops.backend.entity.IncidentStatus;

public class IncidentResponse {

    private Long id;
    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentCategory category;
    private IncidentStatus status;
    private String reportedByEmail;
    private String assignedToEmail;

    public IncidentResponse() {
    }

    public IncidentResponse(
            Long id,
            String title,
            String description,
            IncidentSeverity severity,
            IncidentCategory category,
            IncidentStatus status,
            String reportedByEmail,
            String assignedToEmail) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.category = category;
        this.status = status;
        this.reportedByEmail = reportedByEmail;
        this.assignedToEmail = assignedToEmail;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public IncidentCategory getCategory() {
        return category;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public String getReportedByEmail() {
        return reportedByEmail;
    }

    public String getAssignedToEmail() {
        return assignedToEmail;
    }
}
