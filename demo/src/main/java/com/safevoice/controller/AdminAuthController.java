package com.safevoice.controller;

import com.safevoice.dto.AdminLoginRequest;
import com.safevoice.dto.AuthResponse;
import com.safevoice.service.AdminAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.safevoice.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;
import com.safevoice.service.AuditLogService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminAuthController {

    private final AdminAuthService authService;
    private final AuditLogService auditLogService;

    public AdminAuthController(AdminAuthService authService, AuditLogService auditLogService) {
        this.authService = authService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody AdminLoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            // log admin login
            auditLogService.logAction("ADMIN_LOGIN", response.getUsername(), response.getRole(), "Admin logged in");
            return ResponseEntity.ok(new ApiResponse(true, "Login successful", response));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid username or password", null));
        }
    }
}