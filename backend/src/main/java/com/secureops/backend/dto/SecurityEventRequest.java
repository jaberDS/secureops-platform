package com.secureops.backend.dto;

import com.secureops.backend.entity.ThreatSeverity;

public record SecurityEventRequest(
        String eventType,
        String source,
        String actor,
        String ipAddress,
        String description,
        ThreatSeverity severity
) {
}
