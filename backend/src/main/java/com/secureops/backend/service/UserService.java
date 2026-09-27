package com.secureops.backend.service;

import com.secureops.backend.dto.RegisterRequest;
import com.secureops.backend.dto.UpdateUserRequest;
import com.secureops.backend.dto.UserResponse;
import com.secureops.backend.entity.AccountStatus;
import com.secureops.backend.entity.Role;
import com.secureops.backend.entity.User;
import com.secureops.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailNormalizationService emailNormalizationService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailNormalizationService emailNormalizationService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailNormalizationService =
                emailNormalizationService;
    }

    public Optional<User> findByEmail(String email) {

        String normalizedEmail =
                emailNormalizationService.normalize(email);

        return userRepository.findByEmail(normalizedEmail);
    }

    public boolean emailExists(String email) {

        String normalizedEmail =
                emailNormalizationService.normalize(email);

        return userRepository.findByEmail(normalizedEmail)
                .isPresent();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean verifyPassword(
            String password,
            String passwordHash) {

        return passwordEncoder.matches(
                password,
                passwordHash
        );
    }

    public User createUser(RegisterRequest request) {

        String normalizedEmail =
                emailNormalizationService.normalize(
                        request.getEmail()
                );

        User user = new User();

        user.setEmail(normalizedEmail);

        String hashedPassword =
                passwordEncoder.encode(
                        request.getPassword()
                );

        user.setPasswordHash(hashedPassword);
        user.setRole(Role.EMPLOYEE);
        user.setStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }

    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getRole(),
                        user.getStatus()
                ))
                .toList();
    }

    public UserResponse getUserById(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    public User createUserByAdmin(
            String email,
            String password,
            Role role) {

        String normalizedEmail =
                emailNormalizationService.normalize(email);

        if (userRepository.findByEmail(
                normalizedEmail
        ).isPresent()) {

            throw new IllegalArgumentException(
                    "Email already exists"
            );
        }

        User user = new User();

        user.setEmail(normalizedEmail);

        user.setPasswordHash(
                passwordEncoder.encode(password)
        );

        user.setRole(role);
        user.setStatus(AccountStatus.ACTIVE);

        return userRepository.save(user);
    }

    public User updateUserRole(
            Long userId,
            Role role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        /*
         * Security rule:
         *
         * Never remove the last active ADMIN.
         */
        if (user.getRole() == Role.ADMIN &&
                user.getStatus() == AccountStatus.ACTIVE &&
                role != Role.ADMIN) {

            long activeAdminCount =
                    userRepository.countByRoleAndStatus(
                            Role.ADMIN,
                            AccountStatus.ACTIVE
                    );

            if (activeAdminCount <= 1) {

                throw new IllegalArgumentException(
                        "Cannot remove the last active ADMIN role"
                );
            }
        }

        user.setRole(role);

        return userRepository.save(user);
    }

    public User updateUser(
            Long userId,
            UpdateUserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        if (request.getEmail() != null &&
                !request.getEmail().isBlank()) {

            String normalizedEmail =
                    emailNormalizationService.normalize(
                            request.getEmail()
                    );

            if (!normalizedEmail.equals(
                    user.getEmail()
            )) {

                Optional<User> existingUser =
                        userRepository.findByEmail(
                                normalizedEmail
                        );

                if (existingUser.isPresent()) {

                    throw new IllegalArgumentException(
                            "Email already exists"
                    );
                }

                user.setEmail(normalizedEmail);
            }
        }

        if (request.getPassword() != null &&
                !request.getPassword().isBlank()) {

            user.setPasswordHash(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );
        }

        return userRepository.save(user);
    }

    public User updateUserStatus(
            Long userId,
            AccountStatus status) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );

        /*
         * Security rule:
         *
         * Never disable the last active ADMIN.
         */
        if (user.getRole() == Role.ADMIN &&
                user.getStatus() == AccountStatus.ACTIVE &&
                status == AccountStatus.DISABLED) {

            long activeAdminCount =
                    userRepository.countByRoleAndStatus(
                            Role.ADMIN,
                            AccountStatus.ACTIVE
                    );

            if (activeAdminCount <= 1) {

                throw new IllegalArgumentException(
                        "Cannot disable the last active ADMIN"
                );
            }
        }

        user.setStatus(status);

        return userRepository.save(user);
    }
}