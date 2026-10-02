package com.secureops.backend.siem;

import com.secureops.backend.entity.SecurityEvent;

import java.time.LocalDateTime;

public record SiemEvent(
        String application,
        Long eventId,
        String eventType,
        String source,
        String actor,
        String ipAddress,
        String description,
        String severity,
        LocalDateTime timestamp
) {

    public static SiemEvent from(SecurityEvent event) {
        return new SiemEvent(
                "secureops-platform",
                event.getId(),
                event.getEventType(),
                event.getSource(),
                event.getActor(),
                event.getIpAddress(),
                event.getDescription(),
                event.getSeverity() == null
                        ? null
                        : event.getSeverity().name(),
                event.getTimestamp()
        );
    }
}
