package com.secureops.backend.security;

import com.secureops.backend.entity.AccountStatus;
import com.secureops.backend.entity.User;
import com.secureops.backend.repository.UserRepository;
import com.secureops.backend.service.EmailNormalizationService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EmailNormalizationService emailNormalizationService;

    public CustomUserDetailsService(
            UserRepository userRepository,
            EmailNormalizationService emailNormalizationService) {

        this.userRepository = userRepository;
        this.emailNormalizationService =
                emailNormalizationService;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        String normalizedEmail =
                emailNormalizationService.normalize(email);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        )
                );

        boolean disabled =
                user.getStatus() == AccountStatus.DISABLED;

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .disabled(disabled)
                .build();
    }
}