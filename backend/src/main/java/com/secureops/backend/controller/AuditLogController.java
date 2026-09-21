package com.secureops.backend.controller;

import com.secureops.backend.dto.AuditLogResponse;
import com.secureops.backend.service.AuditLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogResponse> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String actor) {

        if (resourceType != null && action != null) {

            return auditLogService
                    .getLogsByResourceTypeAndAction(
                            resourceType,
                            action
                    );
        }

        if (action != null) {

            return auditLogService.getLogsByAction(action);
        }

        if (resourceType != null) {

            return auditLogService
                    .getLogsByResourceType(resourceType);
        }

        if (actor != null) {

            return auditLogService.getLogsByActor(actor);
        }

        return auditLogService.getAllLogs();
    }
}