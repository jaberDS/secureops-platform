package com.secureops.backend.service;

import com.secureops.backend.dto.RegisterRequest;
import com.secureops.backend.entity.Role;
import com.secureops.backend.entity.User;
import com.secureops.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }
    
    public boolean verifyPassword(String password, String passwordHash) {
    return passwordEncoder.matches(password, passwordHash);
}

    public User createUser(RegisterRequest request) {

    User user = new User();

    user.setEmail(request.getEmail());

    String hashedPassword =
            passwordEncoder.encode(request.getPassword());

    user.setPasswordHash(hashedPassword);

    user.setRole(Role.EMPLOYEE);

    return userRepository.save(user);
}

}