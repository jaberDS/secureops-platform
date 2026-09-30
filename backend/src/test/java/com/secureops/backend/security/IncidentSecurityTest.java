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
class IncidentSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    private String createOrUpdateUser(String email, Role role) {

        User user = userRepository.findByEmail(email)
                .orElseGet(User::new);

        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(
                        "Day09IncidentPassword123!"
                )
        );
        user.setRole(role);
        user.setStatus(AccountStatus.ACTIVE);

        userRepository.save(user);

        return email;
    }

    private String login(String email) throws Exception {

        String requestBody = """
                {
                    "email": "%s",
                    "password": "Day09IncidentPassword123!"
                }
                """.formatted(email);

        String response = mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json.get("token").asText();
    }

    @Test
    void adminShouldAccessIncidentList() throws Exception {

        String email = createOrUpdateUser(
                "day09-incident-admin@secureops.local",
                Role.ADMIN
        );

        String token = login(email);

        mockMvc.perform(
                get("/api/incidents")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void securityAnalystShouldAccessIncidentList() throws Exception {

        String email = createOrUpdateUser(
                "day09-incident-analyst@secureops.local",
                Role.SECURITY_ANALYST
        );

        String token = login(email);

        mockMvc.perform(
                get("/api/incidents")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void managerShouldAccessIncidentList() throws Exception {

        String email = createOrUpdateUser(
                "day09-incident-manager@secureops.local",
                Role.MANAGER
        );

        String token = login(email);

        mockMvc.perform(
                get("/api/incidents")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void employeeShouldNotAccessIncidentList() throws Exception {

        String email = createOrUpdateUser(
                "day09-incident-employee@secureops.local",
                Role.EMPLOYEE
        );

        String token = login(email);

        mockMvc.perform(
                get("/api/incidents")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isForbidden());
    }
}