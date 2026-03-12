package com.safevoice.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Root Controller - Handles root "/" endpoint
 * 
 * Provides status information when accessing the backend root URL
 * Used for health checks, status monitoring, and deployment verification
 * 
 * @author Security Team
 * @version 1.0
 */
@RestController
public class RootController {

    /**
     * Root endpoint - GET /
     * 
     * @return Welcome message with service status
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "TRINETRA Backend");
        response.put("version", "1.0.0");
        response.put("environment", "production");
        response.put("message", "Welcome to TRINETRA - Secure Anonymous Whistleblower Reporting Platform");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
