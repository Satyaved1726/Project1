package com.safevoice.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Production-Grade CORS Configuration
 * 
 * Handles Cross-Origin Resource Sharing (CORS) for requests from frontend applications.
 * This configuration is applied globally to all endpoints.
 * 
 * Allowed Origins:
 * - https://projectfrontend17.vercel.app (Production Frontend)
 * - http://localhost:3000 (Local Development)
 * 
 * @author Security Team
 * @version 1.0
 */
@Configuration
public class CorsConfig {

    @Value("${CORS_ALLOWED_ORIGINS:https://projectfrontend17.vercel.app,http://localhost:3000,http://localhost:4200}")
    private String allowedOrigins;

    /**
     * Global CORS Filter Bean
     * 
     * Configures CORS for all endpoints matching path pattern /**
     * 
     * Allowed Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
     * Allowed Headers: All (*)
     * Credentials: Allowed (for JWT tokens)
     * Max Age: 3600 seconds (1 hour)
     * 
     * @return CorsFilter bean configured with production settings
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Parse allowed origins from environment variable
        // Each origin is trimmed to handle spaces in environment variables
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .collect(Collectors.toList());
        
        System.out.println("[CORS] Configured allowed origins: " + origins);
        
        // Set allowed origins
        config.setAllowedOrigins(origins);
        
        // Set allowed HTTP methods
        // OPTIONS is required for preflight requests
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        
        // Allow all request headers
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Expose headers that client needs to access
        // Authorization header needed for JWT tokens
        config.setExposedHeaders(Arrays.asList(
                "Content-Type",
                "Authorization",
                "X-Requested-With",
                "Accept",
                "Origin"
        ));
        
        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        config.setMaxAge(3600L);
        
        // Apply CORS configuration to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}