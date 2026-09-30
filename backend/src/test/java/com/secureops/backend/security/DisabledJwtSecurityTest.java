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

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class DisabledJwtSecurityTest {

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
            "day09-disabled-jwt@secureops.local";

    private static final String PASSWORD =
            "Day09DisabledJwtPassword123!";

    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .build();

        User user =
                userRepository.findByEmail(EMAIL)
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
    void disabledAccountShouldNotUsePreviouslyIssuedJwt()
            throws Exception {

        String loginRequest = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(EMAIL, PASSWORD);

        String loginResponse =
                mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json =
                objectMapper.readTree(loginResponse);

        String token =
                json.get("token").asText();

        /*
         * Disable the account AFTER the JWT was issued.
         *
         * This simulates:
         *
         * 1. User logs in.
         * 2. JWT is issued.
         * 3. Administrator disables the account.
         * 4. User tries to reuse the old JWT.
         */

        User user =
                userRepository.findByEmail(EMAIL)
                        .orElseThrow();

        user.setStatus(AccountStatus.DISABLED);

        userRepository.save(user);

        mockMvc.perform(
                get("/api/admin/users")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isForbidden());
    }
}
