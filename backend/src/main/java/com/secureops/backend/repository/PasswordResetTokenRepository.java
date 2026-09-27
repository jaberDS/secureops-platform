package com.secureops.backend.repository;

import com.secureops.backend.entity.PasswordResetToken;
import com.secureops.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(
            String tokenHash
    );

    void deleteAllByUser(User user);
}