package com.secureops.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RoleTestController {

    @GetMapping("/admin/test")
    public String adminTest() {
        return "ADMIN access granted";
    }

    @GetMapping("/security/test")
    public String securityTest() {
        return "SECURITY_ANALYST access granted";
    }

    @GetMapping("/manager/test")
    public String managerTest() {
        return "MANAGER access granted";
    }

    @GetMapping("/employee/test")
    public String employeeTest() {
        return "Authenticated user access granted";
    }
}