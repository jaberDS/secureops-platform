package com.secureops.backend.security;

import com.secureops.backend.dto.SecurityAlertResponse;
import com.secureops.backend.dto.SecurityEventRequest;
import com.secureops.backend.entity.DetectionStatus;
import com.secureops.backend.entity.ThreatSeverity;
import com.secureops.backend.repository.SecurityAlertRepository;
import com.secureops.backend.repository.SecurityEventRepository;
import com.secureops.backend.service.ThreatDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ThreatDetectionServiceTest {

    private SecurityEventRepository eventRepository;
    private SecurityAlertRepository alertRepository;
    private ThreatDetectionService detectionService;

    @BeforeEach
    void setUp() {
        eventRepository = mock(SecurityEventRepository.class);
        alertRepository = mock(SecurityAlertRepository.class);

        detectionService =
                new ThreatDetectionService(
                        eventRepository,
                        alertRepository
                );
    }

    @Test
    void disabledAccountLoginShouldCreateHighAlert() {

        SecurityEventRequest request =
                new SecurityEventRequest(
                        "DISABLED_ACCOUNT_LOGIN",
                        "AUTHENTICATION",
                        "disabled@example.com",
                        "192.168.50.30",
                        "Disabled account attempted login",
                        ThreatSeverity.HIGH
                );

        when(eventRepository.save(any()))
                .thenAnswer(invocation -> {
                    var event = invocation.getArgument(
                            0,
                            com.secureops.backend.entity.SecurityEvent.class
                    );
                    event.setId(1L);
                    return event;
                });

        when(alertRepository.save(any()))
                .thenAnswer(invocation -> {
                    var alert = invocation.getArgument(
                            0,
                            com.secureops.backend.entity.SecurityAlert.class
                    );
                    alert.setId(1L);
                    return alert;
                });

        SecurityAlertResponse response =
                detectionService.processEvent(request);

        assertNotNull(response);
        assertEquals(
                "DISABLED_ACCOUNT_LOGIN",
                response.detectionRule()
        );
        assertEquals(
                ThreatSeverity.HIGH,
                response.severity()
        );
        assertEquals(
                DetectionStatus.NEW,
                response.status()
        );
        assertEquals(20, response.riskScore());

        verify(alertRepository).save(any());
    }

    @Test
    void unauthorizedAccessShouldCreateHighAlert() {

        SecurityEventRequest request =
                new SecurityEventRequest(
                        "UNAUTHORIZED_ACCESS",
                        "AUTHORIZATION",
                        "employee@example.com",
                        "192.168.50.20",
                        "Employee attempted protected resource",
                        ThreatSeverity.HIGH
                );

        when(eventRepository.save(any()))
                .thenAnswer(invocation -> {
                    var event = invocation.getArgument(
                            0,
                            com.secureops.backend.entity.SecurityEvent.class
                    );
                    event.setId(2L);
                    return event;
                });

        when(alertRepository.save(any()))
                .thenAnswer(invocation -> {
                    var alert = invocation.getArgument(
                            0,
                            com.secureops.backend.entity.SecurityAlert.class
                    );
                    alert.setId(2L);
                    return alert;
                });

        SecurityAlertResponse response =
                detectionService.processEvent(request);

        assertNotNull(response);
        assertEquals(
                "UNAUTHORIZED_ACCESS",
                response.detectionRule()
        );
        assertEquals(
                ThreatSeverity.HIGH,
                response.severity()
        );
        assertEquals(20, response.riskScore());

        verify(alertRepository).save(any());
    }

    @Test
    void roleChangedShouldCreateHighAlert() {

        SecurityEventRequest request =
                new SecurityEventRequest(
                        "ROLE_CHANGED",
                        "IAM",
                        "admin@example.com",
                        "192.168.50.10",
                        "User role changed",
                        ThreatSeverity.HIGH
                );

        when(eventRepository.save(any()))
                .thenAnswer(invocation -> {
                    var event = invocation.getArgument(
                            0,
                            com.secureops.backend.entity.SecurityEvent.class
                    );
                    event.setId(3L);
                    return event;
                });

        when(alertRepository.save(any()))
                .thenAnswer(invocation -> {
                    var alert = invocation.getArgument(
                            0,
                            com.secureops.backend.entity.SecurityAlert.class
                    );
                    alert.setId(3L);
                    return alert;
                });

        SecurityAlertResponse response =
                detectionService.processEvent(request);

        assertNotNull(response);
        assertEquals(
                "ROLE_CHANGE",
                response.detectionRule()
        );
        assertEquals(
                ThreatSeverity.HIGH,
                response.severity()
        );
        assertEquals(15, response.riskScore());

        verify(alertRepository).save(any());
    }

    @Test
    void criticalEventShouldCreateCriticalAlert() {

        SecurityEventRequest request =
                new SecurityEventRequest(
                        "MALWARE_DETECTED",
                        "ENDPOINT",
                        "security@example.com",
                        "192.168.50.20",
                        "Malware detected on endpoint",
                        ThreatSeverity.CRITICAL
                );

        when(eventRepository.save(any()))
                .thenAnswer(invocation -> {
                    var event = invocation.getArgument(
                            0,
                            com.secureops.backend.entity.SecurityEvent.class
                    );
                    event.setId(4L);
                    return event;
                });

        when(alertRepository.save(any()))
                .thenAnswer(invocation -> {
                    var alert = invocation.getArgument(
                            0,
                            com.secureops.backend.entity.SecurityAlert.class
                    );
                    alert.setId(4L);
                    return alert;
                });

        SecurityAlertResponse response =
                detectionService.processEvent(request);

        assertNotNull(response);
        assertEquals(
                "CRITICAL_SECURITY_EVENT",
                response.detectionRule()
        );
        assertEquals(
                ThreatSeverity.CRITICAL,
                response.severity()
        );
        assertEquals(25, response.riskScore());

        verify(alertRepository).save(any());
    }

    @Test
    void normalLowSeverityEventShouldNotCreateAlert() {

        SecurityEventRequest request =
                new SecurityEventRequest(
                        "NORMAL_ACTIVITY",
                        "APPLICATION",
                        "user@example.com",
                        "192.168.50.20",
                        "Normal application activity",
                        ThreatSeverity.LOW
                );

        when(eventRepository.save(any()))
                .thenAnswer(invocation -> {
                    var event = invocation.getArgument(
                            0,
                            com.secureops.backend.entity.SecurityEvent.class
                    );
                    event.setId(5L);
                    return event;
                });

        SecurityAlertResponse response =
                detectionService.processEvent(request);

        assertNull(response);

        verify(eventRepository).save(any());
        verify(alertRepository, never()).save(any());
    }

    @Test
    void threeLoginFailuresShouldCreateMediumAlert() {

        SecurityEventRequest request =
                new SecurityEventRequest(
                        "LOGIN_FAILURE",
                        "AUTHENTICATION",
                        "attacker@example.com",
                        "192.168.50.30",
                        "Failed login attempt",
                        ThreatSeverity.MEDIUM
                );

        when(eventRepository.save(any()))
                .thenAnswer(invocation -> {
                    var event = invocation.getArgument(
                            0,
                            com.secureops.backend.entity.SecurityEvent.class
                    );
                    event.setId(6L);
                    return event;
                });

        when(eventRepository
                .findByActorAndEventTypeAndTimestampAfter(
                        eq("attacker@example.com"),
                        eq("LOGIN_FAILURE"),
                        any()
                ))
                .thenReturn(List.of(
                        new com.secureops.backend.entity.SecurityEvent(),
                        new com.secureops.backend.entity.SecurityEvent(),
                        new com.secureops.backend.entity.SecurityEvent()
                ));

        when(alertRepository.save(any()))
                .thenAnswer(invocation -> {
                    var alert = invocation.getArgument(
                            0,
                            com.secureops.backend.entity.SecurityAlert.class
                    );
                    alert.setId(6L);
                    return alert;
                });

        SecurityAlertResponse response =
                detectionService.processEvent(request);

        assertNotNull(response);
        assertEquals(
                "MULTIPLE_LOGIN_FAILURES",
                response.detectionRule()
        );
        assertEquals(
                ThreatSeverity.MEDIUM,
                response.severity()
        );
        assertEquals(10, response.riskScore());

        verify(eventRepository)
                .findByActorAndEventTypeAndTimestampAfter(
                        eq("attacker@example.com"),
                        eq("LOGIN_FAILURE"),
                        any()
                );

        verify(alertRepository).save(any());
    }
}
