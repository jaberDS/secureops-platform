package com.secureops.backend.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.secureops.backend.entity.AccountStatus;
import com.secureops.backend.entity.Incident;
import com.secureops.backend.entity.IncidentCategory;
import com.secureops.backend.entity.IncidentSeverity;
import com.secureops.backend.entity.Role;
import com.secureops.backend.entity.User;
import com.secureops.backend.repository.IncidentRepository;
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
class InvestigationNoteSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    private static final String PASSWORD =
            "Day09InvestigationPassword123!";

    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .webAppContextSetup(webApplicationContext)
                        .apply(springSecurity())
                        .build();
    }

    private User createOrUpdateUser(
            String email,
            Role role) {

        User user =
                userRepository.findByEmail(email)
                        .orElseGet(User::new);

        user.setEmail(email);

        user.setPasswordHash(
                passwordEncoder.encode(PASSWORD)
        );

        user.setRole(role);
        user.setStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }

    private Long createIncident(User reporter) {

        Incident incident =
                new Incident();

        incident.setTitle(
                "Day 09 Investigation Security Test"
        );

        incident.setDescription(
                "Incident created for investigation note authorization tests."
        );

        incident.setSeverity(
                IncidentSeverity.MEDIUM
        );

        incident.setCategory(
                IncidentCategory.UNAUTHORIZED_ACCESS
        );

        incident.setReportedBy(reporter);

        return incidentRepository
                .save(incident)
                .getId();
    }

    private String login(String email)
            throws Exception {

        String requestBody = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(
                        email,
                        PASSWORD
                );

        String response =
                mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json =
                objectMapper.readTree(response);

        return json.get("token").asText();
    }

    @Test
    void adminShouldReadInvestigationNotes()
            throws Exception {

        User user =
                createOrUpdateUser(
                        "day09-note-admin@secureops.local",
                        Role.ADMIN
                );

        Long incidentId =
                createIncident(user);

        String token =
                login(user.getEmail());

        mockMvc.perform(
                get(
                        "/api/incidents/{incidentId}/notes",
                        incidentId
                )
                .header(
                        "Authorization",
                        "Bearer " + token
                )
        )
        .andExpect(status().isOk());
    }

    @Test
    void securityAnalystShouldReadInvestigationNotes()
            throws Exception {

        User user =
                createOrUpdateUser(
                        "day09-note-analyst@secureops.local",
                        Role.SECURITY_ANALYST
                );

        Long incidentId =
                createIncident(user);

        String token =
                login(user.getEmail());

        mockMvc.perform(
                get(
                        "/api/incidents/{incidentId}/notes",
                        incidentId
                )
                .header(
                        "Authorization",
                        "Bearer " + token
                )
        )
        .andExpect(status().isOk());
    }

    @Test
    void managerShouldReadInvestigationNotes()
            throws Exception {

        User user =
                createOrUpdateUser(
                        "day09-note-manager@secureops.local",
                        Role.MANAGER
                );

        Long incidentId =
                createIncident(user);

        String token =
                login(user.getEmail());

        mockMvc.perform(
                get(
                        "/api/incidents/{incidentId}/notes",
                        incidentId
                )
                .header(
                        "Authorization",
                        "Bearer " + token
                )
        )
        .andExpect(status().isOk());
    }

    @Test
    void employeeShouldNotReadInvestigationNotes()
            throws Exception {

        User user =
                createOrUpdateUser(
                        "day09-note-employee-read@secureops.local",
                        Role.EMPLOYEE
                );

        Long incidentId =
                createIncident(user);

        String token =
                login(user.getEmail());

        mockMvc.perform(
                get(
                        "/api/incidents/{incidentId}/notes",
                        incidentId
                )
                .header(
                        "Authorization",
                        "Bearer " + token
                )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void adminShouldCreateInvestigationNote()
            throws Exception {

        User user =
                createOrUpdateUser(
                        "day09-note-admin-create@secureops.local",
                        Role.ADMIN
                );

        Long incidentId =
                createIncident(user);

        String token =
                login(user.getEmail());

        String requestBody = """
                {
                    "content": "Admin investigation note for security testing."
                }
                """;

        mockMvc.perform(
                post(
                        "/api/incidents/{incidentId}/notes",
                        incidentId
                )
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .content(requestBody)
        )
        .andExpect(status().isOk());
    }

    @Test
    void securityAnalystShouldCreateInvestigationNote()
            throws Exception {

        User user =
                createOrUpdateUser(
                        "day09-note-analyst-create@secureops.local",
                        Role.SECURITY_ANALYST
                );

        Long incidentId =
                createIncident(user);

        String token =
                login(user.getEmail());

        String requestBody = """
                {
                    "content": "Security analyst investigation note for security testing."
                }
                """;

        mockMvc.perform(
                post(
                        "/api/incidents/{incidentId}/notes",
                        incidentId
                )
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .content(requestBody)
        )
        .andExpect(status().isOk());
    }

    @Test
    void managerShouldNotCreateInvestigationNote()
            throws Exception {

        User user =
                createOrUpdateUser(
                        "day09-note-manager-create@secureops.local",
                        Role.MANAGER
                );

        Long incidentId =
                createIncident(user);

        String token =
                login(user.getEmail());

        String requestBody = """
                {
                    "content": "Manager should not be allowed to create this note."
                }
                """;

        mockMvc.perform(
                post(
                        "/api/incidents/{incidentId}/notes",
                        incidentId
                )
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .content(requestBody)
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void employeeShouldNotCreateInvestigationNote()
            throws Exception {

        User user =
                createOrUpdateUser(
                        "day09-note-employee-create@secureops.local",
                        Role.EMPLOYEE
                );

        Long incidentId =
                createIncident(user);

        String token =
                login(user.getEmail());

        String requestBody = """
                {
                    "content": "Employee should not be allowed to create this note."
                }
                """;

        mockMvc.perform(
                post(
                        "/api/incidents/{incidentId}/notes",
                        incidentId
                )
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .contentType(
                        MediaType.APPLICATION_JSON
                )
                .content(requestBody)
        )
        .andExpect(status().isForbidden());
    }
}