package com.secureops.backend.controller;

import com.secureops.backend.dto.SecurityAlertResponse;
import com.secureops.backend.dto.SecurityEventRequest;
import com.secureops.backend.service.ThreatDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security")
public class ThreatDetectionController {

    private final ThreatDetectionService detectionService;

    public ThreatDetectionController(
            ThreatDetectionService detectionService) {

        this.detectionService = detectionService;
    }

    @PostMapping("/events")
    public ResponseEntity<SecurityAlertResponse> processEvent(
            @RequestBody SecurityEventRequest request) {

        SecurityAlertResponse alert =
                detectionService.processEvent(request);

        if (alert == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(alert);
    }

    @GetMapping("/alerts")
    public List<SecurityAlertResponse> getAlerts() {

        return detectionService.getAllAlerts();
    }

    @GetMapping("/alerts/{id}")
    public SecurityAlertResponse getAlert(
            @PathVariable Long id) {

        return detectionService.getAlert(id);
    }

    @GetMapping("/risk")
    public int getRiskScore() {

        return detectionService.calculateRiskScore();
    }
}
