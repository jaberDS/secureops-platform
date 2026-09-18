package com.secureops.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncoderTest {

    private final PasswordEncoder passwordEncoder = new PasswordConfig().passwordEncoder();

    @Test
    void shouldHashAndVerifyPassword() {

        String password = "MyPassword123!";

        String hash = passwordEncoder.encode(password);

        System.out.println("Original password: " + password);
        System.out.println("BCrypt hash: " + hash);

        assertNotEquals(password, hash);

        assertTrue(passwordEncoder.matches(password, hash));

        assertFalse(passwordEncoder.matches("WrongPassword", hash));
    }
}