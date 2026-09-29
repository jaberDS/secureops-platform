package com.secureops.backend.service;

import com.secureops.backend.dto.AssignIncidentRequest;
import com.secureops.backend.dto.IncidentResponse;
import com.secureops.backend.entity.Incident;
import com.secureops.backend.entity.IncidentStatus;
import com.secureops.backend.entity.Role;
import com.secureops.backend.entity.User;
import com.secureops.backend.repository.IncidentRepository;
import com.secureops.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public IncidentService(
            IncidentRepository incidentRepository,
            UserRepository userRepository,
            AuditLogService auditLogService) {

        this.incidentRepository = incidentRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> getAllIncidents() {

        return incidentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public IncidentResponse createIncident(
            Incident incident,
            String reporterEmail) {

        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        ));

        incident.setReportedBy(reporter);
        incident.setStatus(IncidentStatus.OPEN);

        Incident savedIncident =
                incidentRepository.save(incident);

        auditLogService.log(
                reporterEmail,
                "CREATE",
                "INCIDENT",
                savedIncident.getId(),
                "Incident created"
        );

        return toResponse(savedIncident);
    }

    @Transactional
    public IncidentResponse assignIncident(
            Long incidentId,
            AssignIncidentRequest request,
            String actorEmail) {

        Incident incident =
                incidentRepository.findById(incidentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Incident not found"
                                ));

        User analyst =
                userRepository
                        .findByEmail(request.getAnalystEmail())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Security analyst not found"
                                ));

        if (analyst.getRole() != Role.SECURITY_ANALYST) {
            throw new IllegalArgumentException(
                    "Incident can only be assigned to a security analyst"
            );
        }

        incident.setAssignedTo(analyst);

        if (incident.getStatus() == IncidentStatus.OPEN) {
            incident.setStatus(IncidentStatus.ASSIGNED);
        }

        Incident savedIncident =
                incidentRepository.save(incident);

        auditLogService.log(
                actorEmail,
                "ASSIGN",
                "INCIDENT",
                savedIncident.getId(),
                "Incident assigned to " + analyst.getEmail()
        );

        return toResponse(savedIncident);
    }

    @Transactional
    public IncidentResponse updateStatus(
            Long incidentId,
            IncidentStatus newStatus,
            String actorEmail) {

        Incident incident =
                incidentRepository.findById(incidentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Incident not found"
                                ));

        IncidentStatus currentStatus =
                incident.getStatus();

        if (!isValidTransition(
                currentStatus,
                newStatus)) {

            throw new IllegalArgumentException(
                    "Invalid status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }

        incident.setStatus(newStatus);

        Incident savedIncident =
                incidentRepository.save(incident);

        auditLogService.log(
                actorEmail,
                "STATUS_CHANGE",
                "INCIDENT",
                savedIncident.getId(),
                "Status changed from "
                        + currentStatus
                        + " to "
                        + newStatus
        );

        return toResponse(savedIncident);
    }

    private IncidentResponse toResponse(
            Incident incident) {

        String reportedByEmail = null;
        String assignedToEmail = null;

        if (incident.getReportedBy() != null) {
            reportedByEmail =
                    incident.getReportedBy().getEmail();
        }

        if (incident.getAssignedTo() != null) {
            assignedToEmail =
                    incident.getAssignedTo().getEmail();
        }

        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getSeverity(),
                incident.getCategory(),
                incident.getStatus(),
                reportedByEmail,
                assignedToEmail
        );
    }

    private boolean isValidTransition(
            IncidentStatus currentStatus,
            IncidentStatus newStatus) {

        return switch (currentStatus) {

            case OPEN ->
                    newStatus == IncidentStatus.ASSIGNED;

            case ASSIGNED ->
                    newStatus == IncidentStatus.INVESTIGATING;

            case INVESTIGATING ->
                    newStatus == IncidentStatus.CONTAINED;

            case CONTAINED ->
                    newStatus == IncidentStatus.RESOLVED;

            case RESOLVED ->
                    newStatus == IncidentStatus.CLOSED;

            case CLOSED ->
                    false;
        };
    }
}