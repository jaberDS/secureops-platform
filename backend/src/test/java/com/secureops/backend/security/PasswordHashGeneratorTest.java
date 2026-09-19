package com.secureops.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHashGeneratorTest {

    @Test
    void generatePasswordHash() {

        PasswordEncoder encoder =
                new PasswordConfig().passwordEncoder();

        String password = "ChooseYourOwnPassword123!";

        String hash = encoder.encode(password);

        System.out.println("BCrypt hash:");
        System.out.println(hash);
    }
}