package com.safevoice.controller;

import com.safevoice.dto.UserSignupRequest;
import com.safevoice.dto.UserLoginRequest;
import com.safevoice.dto.AuthResponse;
import com.safevoice.dto.ApiResponse;
import com.safevoice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 * 
 * Handles user authentication operations:
 * - User registration (signup)
 * - User login
 * 
 * Both endpoints are publicly accessible (no authentication required).
 * CORS is configured globally in CorsConfig and SecurityConfig.
 * Individual @CrossOrigin annotation provides additional safety.
 * 
 * @author Security Team
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {
        "https://projectfrontend17.vercel.app",
        "http://localhost:3000",
        "http://localhost:4200"
}, methods = {
        RequestMethod.GET,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.DELETE,
        RequestMethod.OPTIONS,
        RequestMethod.PATCH
}, allowedHeaders = "*", allowCredentials = "true", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * User signup endpoint
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        try {
            AuthResponse response = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "User registered successfully", response));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, ex.getMessage(), null));
        }
    }

    /**
     * User login endpoint
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody UserLoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(new ApiResponse(true, "Login successful", response));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid email or password", null));
        }
    }
}
