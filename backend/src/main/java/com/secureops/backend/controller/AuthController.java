package com.secureops.backend.controller;

import com.secureops.backend.dto.LoginRequest;
import com.secureops.backend.dto.LoginResponse;
import com.secureops.backend.dto.PasswordResetConfirmRequest;
import com.secureops.backend.dto.PasswordResetRequest;
import com.secureops.backend.dto.RegisterRequest;
import com.secureops.backend.security.JwtService;
import com.secureops.backend.service.AuditLogService;
import com.secureops.backend.service.EmailNormalizationService;
import com.secureops.backend.service.PasswordResetService;
import com.secureops.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final EmailNormalizationService emailNormalizationService;
    private final PasswordResetService passwordResetService;

    public AuthController(
            UserService userService,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AuditLogService auditLogService,
            EmailNormalizationService emailNormalizationService,
            PasswordResetService passwordResetService) {

        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
        this.emailNormalizationService = emailNormalizationService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        String normalizedEmail =
                emailNormalizationService.normalize(
                        request.getEmail()
                );

        if (userService.emailExists(normalizedEmail)) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already exists");
        }

        userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        String normalizedEmail =
                emailNormalizationService.normalize(
                        request.getEmail()
                );

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            request.getPassword()
                    )
            );

            auditLogService.logSecurityEvent(
                    normalizedEmail,
                    "LOGIN_SUCCESS",
                    "Successful authentication"
            );

            String token =
                    jwtService.generateToken(
                            normalizedEmail
                    );

            return ResponseEntity.ok(
                    new LoginResponse(token)
            );

        } catch (AuthenticationException exception) {

            /*
             * Keep authentication errors generic.
             *
             * Do not tell the client whether the
             * email exists, is disabled, or has
             * an incorrect password.
             */
            auditLogService.logSecurityEvent(
                    normalizedEmail,
                    "LOGIN_FAILURE",
                    "Authentication failed"
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<String> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request) {

        String resetToken =
                passwordResetService.requestPasswordReset(
                        request.getEmail()
                );

        /*
         * The service normally returns no token.
         *
         * In local development, the token can
         * optionally be exposed through an
         * environment variable so the flow can
         * be tested without an email server.
         */
        if (resetToken != null) {

            return ResponseEntity.ok(
                    "If the account exists, a password reset token has been generated. "
                            + "Development token: "
                            + resetToken
            );
        }

        return ResponseEntity.ok(
                "If the account exists, a password reset token has been generated."
        );
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<String> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request) {

        passwordResetService.resetPassword(
                request.getToken(),
                request.getPassword()
        );

        return ResponseEntity.ok(
                "Password reset successful"
        );
    }
}