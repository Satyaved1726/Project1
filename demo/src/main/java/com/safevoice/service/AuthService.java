package com.safevoice.service;

import com.safevoice.dto.UserSignupRequest;
import com.safevoice.dto.UserLoginRequest;
import com.safevoice.dto.AuthResponse;
import com.safevoice.exception.DuplicateResourceException;
import com.safevoice.exception.ResourceNotFoundException;
import com.safevoice.model.User;
import com.safevoice.repository.UserRepository;
import com.safevoice.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                      JwtUtil jwtUtil, AuthenticationManager authenticationManager,
                      AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.auditLogService = auditLogService;
    }

    public AuthResponse signup(UserSignupRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        // Log the signup action
        auditLogService.logAction(savedUser.getEmail(), "USER", "SIGNUP", 
                "User signed up with email: " + savedUser.getEmail());

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());

        return new AuthResponse(token, savedUser.getEmail(), savedUser.getRole());
    }

    public AuthResponse login(UserLoginRequest request) {
        try {
            // Authenticate user with email and password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Log the login action
            auditLogService.logAction(user.getEmail(), user.getRole(), "LOGIN",
                    "User logged in successfully");

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

            return new AuthResponse(token, user.getEmail(), user.getRole());
        } catch (AuthenticationException e) {
            auditLogService.logAction(request.getEmail(), "USER", "LOGIN_FAILED",
                    "Login attempt failed for email: " + request.getEmail());
            throw new RuntimeException("Invalid email or password");
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
