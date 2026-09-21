package com.secureops.backend.controller;

import com.secureops.backend.dto.CreateUserRequest;
import com.secureops.backend.dto.UpdateUserRequest;
import com.secureops.backend.dto.UpdateUserRoleRequest;
import com.secureops.backend.dto.UpdateUserStatusRequest;
import com.secureops.backend.dto.UserResponse;
import com.secureops.backend.entity.User;
import com.secureops.backend.service.AuditLogService;
import com.secureops.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    public UserController(
            UserService userService,
            AuditLogService auditLogService) {

        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public UserResponse getUserById(
            @PathVariable Long userId) {

        return userService.getUserById(userId);
    }

    @PostMapping
    public UserResponse createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {

        User user = userService.createUserByAdmin(
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );

        auditLogService.log(
                authentication.getName(),
                "USER_CREATED",
                "USER",
                user.getId(),
                "Created user: " + user.getEmail()
                        + " with role: " + user.getRole()
        );

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    @PatchMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        User user = userService.updateUser(
                userId,
                request
        );

        auditLogService.log(
                authentication.getName(),
                "USER_UPDATED",
                "USER",
                user.getId(),
                "Updated user information for: "
                        + user.getEmail()
        );

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    @PatchMapping("/{userId}/role")
    public UserResponse updateUserRole(
            @PathVariable Long userId,
            @RequestBody UpdateUserRoleRequest request,
            Authentication authentication) {

        User user = userService.updateUserRole(
                userId,
                request.getRole()
        );

        auditLogService.log(
                authentication.getName(),
                "ROLE_CHANGED",
                "USER",
                user.getId(),
                "Changed role for user: "
                        + user.getEmail()
                        + " to: "
                        + user.getRole()
        );

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    @PatchMapping("/{userId}/status")
    public UserResponse updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request,
            Authentication authentication) {

        User user = userService.updateUserStatus(
                userId,
                request.getStatus()
        );

        String action;

        if (user.getStatus().name().equals("DISABLED")) {
            action = "USER_DISABLED";
        } else {
            action = "USER_ENABLED";
        }

        auditLogService.log(
                authentication.getName(),
                action,
                "USER",
                user.getId(),
                "Changed account status for "
                        + user.getEmail()
                        + " to: "
                        + user.getStatus()
        );

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }
}