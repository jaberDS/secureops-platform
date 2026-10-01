package com.secureops.backend.security;

import com.secureops.backend.entity.AccountStatus;
import com.secureops.backend.entity.SecurityEvent;
import com.secureops.backend.entity.User;
import com.secureops.backend.repository.SecurityEventRepository;
import com.secureops.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ThreatDetectionDebugTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Test
    void inspectLoginFailureEvents() {

        String email =
                "day11-detection-login@secureops.local";

        User user =
                userRepository.findByEmail(email)
                        .orElse(null);

        System.out.println(
                "USER EXISTS = " + (user != null)
        );

        List<SecurityEvent> events =
                securityEventRepository.findAll()
                        .stream()
                        .filter(event ->
                                "LOGIN_FAILURE"
                                        .equals(event.getEventType()))
                        .filter(event ->
                                email.equals(event.getActor()))
                        .toList();

        System.out.println(
                "LOGIN_FAILURE EVENTS = "
                        + events.size()
        );

        events.forEach(event ->
                System.out.println(
                        "EVENT: id="
                                + event.getId()
                                + ", type="
                                + event.getEventType()
                                + ", actor="
                                + event.getActor()
                                + ", timestamp="
                                + event.getTimestamp()
                )
        );

        assertTrue(true);
    }
}
