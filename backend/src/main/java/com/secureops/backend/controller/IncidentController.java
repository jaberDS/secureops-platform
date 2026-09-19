package com.secureops.backend.controller;

import com.secureops.backend.dto.AssignIncidentRequest;
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

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public List<Incident> getAllIncidents() {
        return incidentService.getAllIncidents();
    }

    @PostMapping
    public Incident createIncident(
            @Valid @RequestBody Incident incident,
            Authentication authentication) {

        String reporterEmail = authentication.getName();

        return incidentService.createIncident(
                incident,
                reporterEmail
        );
    }

    @PatchMapping("/{id}/status")
    public Incident updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncidentStatusRequest request) {

        return incidentService.updateStatus(
                id,
                request.getStatus()
        );
    }

    @PatchMapping("/{id}/assign")
    public Incident assignIncident(
            @PathVariable Long id,
            @Valid @RequestBody AssignIncidentRequest request) {

        return incidentService.assignIncident(
                id,
                request
        );
    }
}