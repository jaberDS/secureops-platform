package com.secureops.backend.service;

import com.secureops.backend.dto.AuditLogResponse;
import com.secureops.backend.entity.AuditLog;
import com.secureops.backend.entity.User;
import com.secureops.backend.repository.AuditLogRepository;
import com.secureops.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository) {

        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
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

        return auditLogRepository.save(auditLog);
    }

    public List<AuditLogResponse> getAllLogs() {

        return auditLogRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AuditLogResponse> getLogsByAction(String action) {

        return auditLogRepository.findByAction(action)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AuditLogResponse> getLogsByResourceType(
            String resourceType) {

        return auditLogRepository.findByResourceType(resourceType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AuditLogResponse> getLogsByActor(String actor) {

        return auditLogRepository.findByUser_Email(actor)
                .stream()
                .map(this::toResponse)
                .toList();
    }

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

    private AuditLogResponse toResponse(AuditLog auditLog) {

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