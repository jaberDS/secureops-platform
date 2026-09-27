package com.secureops.backend.service;

import com.secureops.backend.entity.AccountStatus;
import com.secureops.backend.entity.PasswordResetToken;
import com.secureops.backend.entity.User;
import com.secureops.backend.repository.PasswordResetTokenRepository;
import com.secureops.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNormalizationService emailNormalizationService;
    private final AuditLogService auditLogService;

    private final long expirationMinutes;
    private final boolean exposeToken;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailNormalizationService emailNormalizationService,
            AuditLogService auditLogService,
            @Value("${security.password-reset.expiration-minutes:15}")
            long expirationMinutes,
            @Value("${security.password-reset.expose-token:false}")
            boolean exposeToken) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailNormalizationService =
                emailNormalizationService;
        this.auditLogService = auditLogService;
        this.expirationMinutes = expirationMinutes;
        this.exposeToken = exposeToken;
    }

    @Transactional
    public String requestPasswordReset(String email) {

        String normalizedEmail =
                emailNormalizationService.normalize(email);

        Optional<User> optionalUser =
                userRepository.findByEmail(normalizedEmail);

        /*
         * Always behave generically when the account
         * does not exist.
         *
         * This prevents account enumeration.
         */
        if (optionalUser.isEmpty()) {
            return null;
        }

        User user = optionalUser.get();

        /*
         * Disabled accounts cannot perform a
         * password reset.
         */
        if (user.getStatus() != AccountStatus.ACTIVE) {
            return null;
        }

        /*
         * Invalidate previous reset tokens.
         */
        tokenRepository.deleteAllByUser(user);

        String rawToken = generateToken();

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setUser(user);
        resetToken.setTokenHash(
                hashToken(rawToken)
        );
        resetToken.setCreatedAt(
                Instant.now()
        );
        resetToken.setExpiresAt(
                Instant.now().plus(
                        expirationMinutes,
                        ChronoUnit.MINUTES
                )
        );

        tokenRepository.save(resetToken);

        auditLogService.logSecurityEvent(
                normalizedEmail,
                "PASSWORD_RESET_REQUESTED",
                "Password reset requested"
        );

        /*
         * In production, this token should be sent
         * through a secure email link.
         *
         * For this local lab, the token can be
         * exposed only when explicitly enabled.
         */
        if (exposeToken) {
            return rawToken;
        }

        return null;
    }

    @Transactional
    public void resetPassword(
            String rawToken,
            String newPassword) {

        String tokenHash =
                hashToken(rawToken);

        PasswordResetToken resetToken =
                tokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid or expired password reset token"
                                )
                        );

        if (resetToken.getUsedAt() != null) {

            throw new IllegalArgumentException(
                    "Invalid or expired password reset token"
            );
        }

        if (resetToken.getExpiresAt()
                .isBefore(Instant.now())) {

            throw new IllegalArgumentException(
                    "Invalid or expired password reset token"
            );
        }

        User user = resetToken.getUser();

        if (user.getStatus() != AccountStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Invalid or expired password reset token"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        resetToken.setUsedAt(
                Instant.now()
        );

        tokenRepository.save(resetToken);

        auditLogService.logSecurityEvent(
                user.getEmail(),
                "PASSWORD_RESET_SUCCESS",
                "Password reset completed"
        );
    }

    private String generateToken() {

        byte[] randomBytes =
                new byte[32];

        SECURE_RANDOM.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "Unable to securely hash reset token",
                    exception
            );
        }
    }
}