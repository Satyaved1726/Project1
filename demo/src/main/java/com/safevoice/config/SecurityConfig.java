package com.safevoice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.safevoice.security.CustomUserDetailsService;
import com.safevoice.security.JwtAuthenticationFilter;

/**
 * Production-Grade Security Configuration
 * 
 * Implements Spring Security 6+ best practices:
 * - CORS enabled globally
 * - CSRF disabled for stateless JWT authentication
 * - Session-less (Stateless) configuration
 * - JWT filter chain
 * - Role-based authorization
 * 
 * @author Security Team
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Password encoder bean using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication provider using custom user details service
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Main Security Filter Chain
     * 
     * Configuration:
     * 1. Enable CORS with default settings (uses CorsConfigurationSource from CorsConfig)
     * 2. Disable CSRF (stateless JWT-based authentication)
     * 3. Stateless session management (no server-side sessions)
     * 4. Authorization rules:
     *    - /api/auth/** endpoints are PUBLIC
     *    - /api/reports/submit is PUBLIC
     *    - /api/reports/token/** is PUBLIC
     *    - /api/admin/** requires ADMIN role
     *    - All other endpoints require authentication
     * 5. JWT filter is applied before username/password filter
     * 
     * @param http HttpSecurity to configure
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS is handled by GlobalCorsFilter at servlet level (runs BEFORE Spring Security)
                // This ensures CORS headers are applied globally
                
                // Disable CSRF (not needed for stateless JWT authentication)
                .csrf(csrf -> csrf.disable())
                
                // Use stateless session management for REST APIs
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // Authorization configuration
                .authorizeHttpRequests(authz -> authz
                        // Allow preflight OPTIONS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Public root endpoint
                        .requestMatchers(HttpMethod.GET, "/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/").permitAll()
                        
                        // Public endpoints - Authentication endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/auth/**").permitAll()
                        
                        // Public endpoints - Report submission (anonymous)
                        .requestMatchers(HttpMethod.POST, "/api/reports/submit").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reports/token/**").permitAll()
                        
                        // Health check endpoint
                        .requestMatchers("/api/health").permitAll()
                        
                        // Admin endpoints require ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                
                // Register authentication provider
                .authenticationProvider(authenticationProvider())
                
                // Add JWT filter before username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
