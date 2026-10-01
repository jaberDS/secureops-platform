package com.secureops.backend.service;

import com.secureops.backend.dto.AuditLogResponse;
import com.secureops.backend.dto.SecurityEventRequest;
import com.secureops.backend.entity.AuditLog;
import com.secureops.backend.entity.ThreatSeverity;
import com.secureops.backend.entity.User;
import com.secureops.backend.repository.AuditLogRepository;
import com.secureops.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ThreatDetectionService threatDetectionService;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            ThreatDetectionService threatDetectionService) {

        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.threatDetectionService = threatDetectionService;
    }

    public AuditLog log(
            String userEmail,
            String action,
            String resourceType,
            Long resourceId,
            String details) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        ));

        AuditLog auditLog = new AuditLog();

        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setDetails(details);

        return auditLogRepository.save(auditLog);
    }

    public AuditLog logSecurityEvent(
            String userEmail,
            String action,
            String details) {

        AuditLog auditLog = new AuditLog();

        if (userEmail != null && !userEmail.isBlank()) {

            userRepository.findByEmail(userEmail)
                    .ifPresent(auditLog::setUser);
        }

        auditLog.setAction(action);
        auditLog.setResourceType("AUTHENTICATION");
        auditLog.setResourceId(0L);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLog.setDetails(details);

        AuditLog savedAuditLog =
                auditLogRepository.save(auditLog);

        SecurityEventRequest securityEvent =
                new SecurityEventRequest(
                        action,
                        "AUTHENTICATION",
                        userEmail,
                        null,
                        details,
                        determineSeverity(action)
                );

        /*
         * Day 11 Threat Detection integration.
         *
         * Threat detection must not break the existing
         * authentication and audit logging flow.
         *
         * The audit log has already been saved before
         * the detection engine is called.
         */
        try {

            threatDetectionService.processEvent(
                    securityEvent
            );

        } catch (RuntimeException exception) {

            /*
             * Detection failure is isolated from the
             * existing audit/authentication operation.
             */
        }

        return savedAuditLog;
    }

    private ThreatSeverity determineSeverity(
            String action) {

        if (action == null) {
            return ThreatSeverity.LOW;
        }

        return switch (action.toUpperCase()) {

            case "LOGIN_FAILURE" ->
                    ThreatSeverity.MEDIUM;

            case "PASSWORD_RESET_REQUEST" ->
                    ThreatSeverity.MEDIUM;

            case "LOGIN_SUCCESS" ->
                    ThreatSeverity.LOW;

            case "PASSWORD_RESET_SUCCESS" ->
                    ThreatSeverity.LOW;

            default ->
                    ThreatSeverity.LOW;
        };
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getAllLogs() {

        return auditLogRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByAction(
            String action) {

        return auditLogRepository
                .findByAction(action)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByResourceType(
            String resourceType) {

        return auditLogRepository
                .findByResourceType(resourceType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByActor(
            String actor) {

        return auditLogRepository
                .findByUser_Email(actor)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getLogsByResourceTypeAndAction(
            String resourceType,
            String action) {

        return auditLogRepository
                .findByResourceTypeAndAction(
                        resourceType,
                        action
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AuditLogResponse toResponse(
            AuditLog auditLog) {

        String actor = null;

        if (auditLog.getUser() != null) {
            actor = auditLog.getUser().getEmail();
        }

        return new AuditLogResponse(
                auditLog.getId(),
                actor,
                auditLog.getAction(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getTimestamp(),
                auditLog.getDetails()
        );
    }
}

