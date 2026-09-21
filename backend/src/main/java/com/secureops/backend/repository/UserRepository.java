package com.secureops.backend.repository;

import com.secureops.backend.entity.AccountStatus;
import com.secureops.backend.entity.Role;
import com.secureops.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    long countByRoleAndStatus(
            Role role,
            AccountStatus status
    );
}