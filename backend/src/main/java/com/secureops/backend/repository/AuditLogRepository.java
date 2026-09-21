package com.secureops.backend.repository;

import com.secureops.backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByResourceType(String resourceType);

    List<AuditLog> findByUser_Email(String email);

    List<AuditLog> findByResourceTypeAndAction(
            String resourceType,
            String action
    );
}