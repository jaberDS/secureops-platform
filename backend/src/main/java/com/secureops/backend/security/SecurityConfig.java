
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
                // Disable CSRF because we are using JWT
                .csrf(csrf -> csrf.disable())

                // JWT authentication is stateless
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // ==========================================
                        // Authentication
                        // ==========================================
                        .requestMatchers("/api/auth/**")
                        .permitAll()


                        // ==========================================
                        // Investigation Notes
                        // ==========================================

                        // View investigation notes
                        // ADMIN, SECURITY_ANALYST and MANAGER
                        // can read investigation notes.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/incidents/*/notes"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECURITY_ANALYST",
                                "MANAGER"
                        )

                        // Create investigation notes
                        // Only ADMIN and SECURITY_ANALYST
                        // can write investigation notes.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/incidents/*/notes"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECURITY_ANALYST"
                        )


                        // ==========================================
                        // Incident Management
                        // ==========================================

                        // View incidents
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/incidents/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECURITY_ANALYST",
                                "MANAGER"
                        )

                        // Create incidents
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

                        // Update incident status / assignment
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/incidents/**"
                        )
                        .hasAnyRole(
                                "ADMIN",
                                "SECURITY_ANALYST"
                        )


                        // ==========================================
                        // Role-based endpoints
                        // ==========================================

                        // Administrator endpoints
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Security analyst endpoints
                        .requestMatchers("/api/security/**")
                        .hasRole("SECURITY_ANALYST")

                        // Manager endpoints
                        .requestMatchers("/api/manager/**")
                        .hasRole("MANAGER")


                        // ==========================================
                        // Default rule
                        // ==========================================

                        // Everything else requires authentication
                        .anyRequest()
                        .authenticated()
                )

                // JWT filter runs before Spring's authentication filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }


    // ==========================================
    // Authentication Provider
    // ==========================================

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }


    // ==========================================
    // Authentication Manager
    // ==========================================

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }
}

