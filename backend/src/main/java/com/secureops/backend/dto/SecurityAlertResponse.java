package com.secureops.backend.dto;

import com.secureops.backend.entity.DetectionStatus;
import com.secureops.backend.entity.ThreatSeverity;

import java.time.LocalDateTime;

public record SecurityAlertResponse(
        Long id,
        String detectionRule,
        ThreatSeverity severity,
        String title,
        String description,
        Long sourceEventId,
        LocalDateTime timestamp,
        DetectionStatus status,
        Integer riskScore
) {
}
