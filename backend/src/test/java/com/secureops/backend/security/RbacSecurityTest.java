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
class RbacSecurityTest {

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
            "day09-rbac-employee@secureops.local";

    private static final String PASSWORD =
            "Day09RbacPassword123!";

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        /*
         * Reuse the test user if it already exists.
         *
         * We do not delete the user because audit logs may
         * contain records referencing this user's ID.
         */
        User user = userRepository.findByEmail(EMAIL)
                .orElseGet(User::new);

        user.setEmail(EMAIL);

        user.setPasswordHash(
                passwordEncoder.encode(PASSWORD)
        );

        /*
         * This is the important part of the RBAC test.
         *
         * The user has a valid account and valid credentials,
         * but the role is EMPLOYEE, not ADMIN.
         */
        user.setRole(Role.EMPLOYEE);

        user.setStatus(AccountStatus.ACTIVE);

        userRepository.save(user);
    }

    @Test
    void employeeWithValidJwtShouldNotAccessAdminEndpoint()
            throws Exception {

        /*
         * Step 1:
         * Login using the real authentication endpoint.
         *
         * This gives us a real JWT.
         */
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

        /*
         * Step 2:
         * Extract the JWT from the login response.
         */
        JsonNode json =
                objectMapper.readTree(loginResponse);

        String token =
                json.get("token").asText();

        /*
         * Step 3:
         * Use the valid JWT to access an ADMIN endpoint.
         *
         * The user is authenticated,
         * but the user has the EMPLOYEE role.
         */
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