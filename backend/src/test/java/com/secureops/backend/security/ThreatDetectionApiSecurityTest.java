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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ThreatDetectionApiSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private static final String ANALYST_EMAIL =
            "day11-security-analyst@secureops.local";

    private static final String EMPLOYEE_EMAIL =
            "day11-security-employee@secureops.local";

    private static final String PASSWORD =
            "Day11SecurityPassword123!";

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        User analyst =
                userRepository.findByEmail(ANALYST_EMAIL)
                        .orElseGet(User::new);

        analyst.setEmail(ANALYST_EMAIL);
        analyst.setPasswordHash(
                passwordEncoder.encode(PASSWORD)
        );
        analyst.setRole(Role.SECURITY_ANALYST);
        analyst.setStatus(AccountStatus.ACTIVE);

        userRepository.save(analyst);

        User employee =
                userRepository.findByEmail(EMPLOYEE_EMAIL)
                        .orElseGet(User::new);

        employee.setEmail(EMPLOYEE_EMAIL);
        employee.setPasswordHash(
                passwordEncoder.encode(PASSWORD)
        );
        employee.setRole(Role.EMPLOYEE);
        employee.setStatus(AccountStatus.ACTIVE);

        userRepository.save(employee);
    }

    @Test
    void securityAnalystShouldAccessSecurityAlerts()
            throws Exception {

        String token = loginAndGetToken(
                ANALYST_EMAIL
        );

        mockMvc.perform(
                get("/api/security/alerts")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void securityAnalystShouldAccessRiskEndpoint()
            throws Exception {

        String token = loginAndGetToken(
                ANALYST_EMAIL
        );

        mockMvc.perform(
                get("/api/security/risk")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void employeeShouldNotAccessSecurityAlerts()
            throws Exception {

        String token = loginAndGetToken(
                EMPLOYEE_EMAIL
        );

        mockMvc.perform(
                get("/api/security/alerts")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void employeeShouldNotAccessRiskEndpoint()
            throws Exception {

        String token = loginAndGetToken(
                EMPLOYEE_EMAIL
        );

        mockMvc.perform(
                get("/api/security/risk")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserShouldNotAccessSecurityAlerts()
            throws Exception {

        mockMvc.perform(
                get("/api/security/alerts")
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedUserShouldNotAccessRiskEndpoint()
            throws Exception {

        mockMvc.perform(
                get("/api/security/risk")
        )
        .andExpect(status().isForbidden());
    }

    private String loginAndGetToken(String email)
            throws Exception {

        String loginRequest = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                        email,
                        PASSWORD
                );

        String loginResponse =
                mockMvc.perform(
                        org.springframework.test.web.servlet.request
                                .MockMvcRequestBuilders
                                .post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(loginRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json =
                objectMapper.readTree(loginResponse);

        return json.get("token").asText();
    }
}
