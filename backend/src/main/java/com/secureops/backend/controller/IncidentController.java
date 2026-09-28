package com.secureops.backend.controller;

import com.secureops.backend.dto.AssignIncidentRequest;
import com.secureops.backend.dto.IncidentResponse;
import com.secureops.backend.dto.UpdateIncidentStatusRequest;
import com.secureops.backend.entity.Incident;
import com.secureops.backend.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(
            IncidentService incidentService) {

        this.incidentService = incidentService;
    }

    @GetMapping
    public List<IncidentResponse> getAllIncidents() {

        return incidentService.getAllIncidents();
    }

    @PostMapping
    public IncidentResponse createIncident(
            @Valid @RequestBody Incident incident,
            Authentication authentication) {

        String reporterEmail =
                authentication.getName();

        return incidentService.createIncident(
                incident,
                reporterEmail
        );
    }

    @PatchMapping("/{id}/status")
    public IncidentResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncidentStatusRequest request,
            Authentication authentication) {

        String actorEmail =
                authentication.getName();

        return incidentService.updateStatus(
                id,
                request.getStatus(),
                actorEmail
        );
    }

    @PatchMapping("/{id}/assign")
    public IncidentResponse assignIncident(
            @PathVariable Long id,
            @Valid @RequestBody AssignIncidentRequest request,
            Authentication authentication) {

        String actorEmail =
                authentication.getName();

        return incidentService.assignIncident(
                id,
                request,
                actorEmail
        );
    }
}
