package com.secureops.backend.service;

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

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}