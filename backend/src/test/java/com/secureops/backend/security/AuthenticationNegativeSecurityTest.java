package com.secureops.backend.security;

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
class AuthenticationNegativeSecurityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    private static final String EMAIL =
            "day09-disabled-test@secureops.local";

    private static final String PASSWORD =
            "Day09DisabledPassword123!";

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

        user.setStatus(AccountStatus.DISABLED);

        userRepository.save(user);
    }

    @Test
    void disabledAccountShouldNotLogin()
            throws Exception {

        String requestBody = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(EMAIL, PASSWORD);

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwtShouldNotAccessProtectedEndpoint()
            throws Exception {

        mockMvc.perform(
                get("/api/admin/users")
                        .header(
                                "Authorization",
                                "Bearer this.is.not.a.valid.jwt"
                        )
        )
        .andExpect(status().isForbidden());
    }
}
