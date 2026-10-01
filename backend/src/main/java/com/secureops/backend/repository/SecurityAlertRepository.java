package com.secureops.backend.repository;

import com.secureops.backend.entity.SecurityAlert;
import com.secureops.backend.entity.DetectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityAlertRepository
        extends JpaRepository<SecurityAlert, Long> {

    List<SecurityAlert> findAllByOrderByTimestampDesc();

    List<SecurityAlert> findByStatusOrderByTimestampDesc(
            DetectionStatus status
    );
}
