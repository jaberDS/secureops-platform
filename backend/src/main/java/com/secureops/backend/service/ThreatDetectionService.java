package com.secureops.backend.service;

import com.secureops.backend.dto.SecurityAlertResponse;
import com.secureops.backend.dto.SecurityEventRequest;
import com.secureops.backend.entity.*;
import com.secureops.backend.repository.SecurityAlertRepository;
import com.secureops.backend.repository.SecurityEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ThreatDetectionService {

    private final SecurityEventRepository eventRepository;
    private final SecurityAlertRepository alertRepository;

    public ThreatDetectionService(
            SecurityEventRepository eventRepository,
            SecurityAlertRepository alertRepository) {

        this.eventRepository = eventRepository;
        this.alertRepository = alertRepository;
    }

    public SecurityAlertResponse processEvent(
            SecurityEventRequest request) {

        SecurityEvent event = new SecurityEvent();

        event.setEventType(request.eventType());
        event.setSource(request.source());
        event.setActor(request.actor());
        event.setIpAddress(request.ipAddress());
        event.setDescription(request.description());
        event.setSeverity(
                request.severity() == null
                        ? ThreatSeverity.LOW
                        : request.severity()
        );
        event.setTimestamp(LocalDateTime.now());

        event = eventRepository.save(event);

        DetectionResult result = detect(event);

        if (result == null) {
            return null;
        }

        SecurityAlert alert = new SecurityAlert();

        alert.setDetectionRule(result.rule());
        alert.setSeverity(result.severity());
        alert.setTitle(result.title());
        alert.setDescription(result.description());
        alert.setSourceEvent(event);
        alert.setTimestamp(LocalDateTime.now());
        alert.setStatus(DetectionStatus.NEW);
        alert.setRiskScore(result.score());

        alert = alertRepository.save(alert);

        return toResponse(alert);
    }

    private DetectionResult detect(
            SecurityEvent event) {

        String type =
                event.getEventType()
                        .toUpperCase();

        if (type.equals("LOGIN_FAILURE")) {

            LocalDateTime window =
                    LocalDateTime.now().minusMinutes(10);

            long failures =
                    eventRepository
                            .findByActorAndEventTypeAndTimestampAfter(
                                    event.getActor(),
                                    "LOGIN_FAILURE",
                                    window
                            )
                            .size();

            if (failures >= 3) {

                return new DetectionResult(
                        "MULTIPLE_LOGIN_FAILURES",
                        ThreatSeverity.MEDIUM,
                        "Repeated authentication failures",
                        "Multiple failed login attempts detected for the same actor.",
                        10
                );
            }
        }

        if (type.equals("DISABLED_ACCOUNT_LOGIN")) {

            return new DetectionResult(
                    "DISABLED_ACCOUNT_LOGIN",
                    ThreatSeverity.HIGH,
                    "Disabled account login attempt",
                    "A disabled account attempted to authenticate.",
                    20
            );
        }

        if (type.equals("UNAUTHORIZED_ACCESS")) {

            return new DetectionResult(
                    "UNAUTHORIZED_ACCESS",
                    ThreatSeverity.HIGH,
                    "Unauthorized access attempt",
                    "A protected resource was accessed without sufficient permission.",
                    20
            );
        }

        if (type.equals("ROLE_CHANGED")) {

            return new DetectionResult(
                    "ROLE_CHANGE",
                    ThreatSeverity.HIGH,
                    "Role or permission change",
                    "A user's security role was changed.",
                    15
            );
        }

        if (type.equals("ADMIN_ACTIVITY")) {

            return new DetectionResult(
                    "SUSPICIOUS_ADMIN_ACTIVITY",
                    ThreatSeverity.HIGH,
                    "Suspicious administrative activity",
                    "Administrative activity requires security review.",
                    15
            );
        }

        if (event.getSeverity() == ThreatSeverity.CRITICAL) {

            return new DetectionResult(
                    "CRITICAL_SECURITY_EVENT",
                    ThreatSeverity.CRITICAL,
                    "Critical security event",
                    event.getDescription(),
                    25
            );
        }

        return null;
    }

    public List<SecurityAlertResponse> getAllAlerts() {

        return alertRepository
                .findAllByOrderByTimestampDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SecurityAlertResponse getAlert(Long id) {

        return alertRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Security alert not found"
                        )
                );
    }

    public int calculateRiskScore() {

        LocalDateTime window =
                LocalDateTime.now().minusHours(1);

        return Math.min(
                100,
                alertRepository
                        .findAllByOrderByTimestampDesc()
                        .stream()
                        .filter(alert ->
                                alert.getTimestamp()
                                        .isAfter(window))
                        .mapToInt(SecurityAlert::getRiskScore)
                        .sum()
        );
    }

    private SecurityAlertResponse toResponse(
            SecurityAlert alert) {

        return new SecurityAlertResponse(
                alert.getId(),
                alert.getDetectionRule(),
                alert.getSeverity(),
                alert.getTitle(),
                alert.getDescription(),
                alert.getSourceEvent() == null
                        ? null
                        : alert.getSourceEvent().getId(),
                alert.getTimestamp(),
                alert.getStatus(),
                alert.getRiskScore()
        );
    }

    private record DetectionResult(
            String rule,
            ThreatSeverity severity,
            String title,
            String description,
            int score
    ) {
    }
}
