package com.secureops.backend.repository;

import com.secureops.backend.entity.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SecurityEventRepository
        extends JpaRepository<SecurityEvent, Long> {

    List<SecurityEvent> findByEventTypeAndTimestampAfter(
            String eventType,
            LocalDateTime timestamp
    );

    List<SecurityEvent> findByActorAndEventTypeAndTimestampAfter(
            String actor,
            String eventType,
            LocalDateTime timestamp
    );
}
