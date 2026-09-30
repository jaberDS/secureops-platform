package com.secureops.backend.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureops.backend.entity.AccountStatus;
import com.secureops.backend.entity.Role;
import com.secureops.backend.entity.User;
import com.secureops.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthenticationSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private static final String EMAIL =
            "day09-auth-test@secureops.local";

    private static final String PASSWORD =
            "Day09Password123!";

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        /*
         * Reuse the existing test user if it already exists.
         *
         * We do NOT delete the user because audit_logs may contain
         * records referencing this user's ID.
         */
        User user = userRepository.findByEmail(EMAIL)
                .orElseGet(User::new);

        user.setEmail(EMAIL);

        user.setPasswordHash(
                passwordEncoder.encode(PASSWORD)
        );

        user.setRole(Role.EMPLOYEE);

        user.setStatus(AccountStatus.ACTIVE);

        userRepository.save(user);
    }

    @Test
    void shouldAuthenticateWithValidCredentials()
            throws Exception {

        String requestBody = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(EMAIL, PASSWORD);

        String response =
                mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json =
                objectMapper.readTree(response);

        String token =
                json.get("token").asText();

        assertNotNull(token);

        assertFalse(token.isBlank());
    }

    @Test
    void shouldRejectInvalidPassword()
            throws Exception {

        String requestBody = """
                {
                    "email": "%s",
                    "password": "WrongPassword123!"
                }
                """.formatted(EMAIL);

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isUnauthorized());
    }
}
