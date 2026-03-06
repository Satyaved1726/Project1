package com.safevoice.service;

import com.safevoice.dto.AdminLoginRequest;
import com.safevoice.dto.AuthResponse;
import com.safevoice.model.AdminUser;
import com.safevoice.repository.AdminUserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;

    public AdminAuthService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    public AuthResponse login(AdminLoginRequest request) {
        AdminUser user = adminUserRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = UUID.randomUUID().toString();
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}