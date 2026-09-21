package com.secureops.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Authentication endpoints
                        .requestMatchers("/api/auth/**")
                        .permitAll()

                        // ADMIN endpoints
                        // This must be protected before generic rules.
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Investigation notes - read access
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/incidents/*/notes"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECURITY_ANALYST",
                                "MANAGER"
                        )

                        // Investigation notes - create access
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/incidents/*/notes"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECURITY_ANALYST"
                        )

                        // Incident read access
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/incidents/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECURITY_ANALYST",
                                "MANAGER"
                        )

                        // Incident creation
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/incidents/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECURITY_ANALYST",
                                "MANAGER",
                                "EMPLOYEE"
                        )

                        // Incident status updates
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/incidents/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECURITY_ANALYST"
                        )

                        // Audit logs
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/audit-logs"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECURITY_ANALYST"
                        )

                        // Security analyst endpoints
                        .requestMatchers("/api/security/**")
                        .hasRole("SECURITY_ANALYST")

                        // Manager endpoints
                        .requestMatchers("/api/manager/**")
                        .hasRole("MANAGER")

                        // Everything else requires authentication
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }
}