package com.safevoice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

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
     * CORS Filter is now handled by GlobalCorsFilter with explicit FilterRegistrationBean
     * This old bean has been disabled to avoid filter conflicts
     * 
     * GlobalCorsFilter ensures:
     * - Filter runs BEFORE Spring Security (order = -100)
     * - Proper origin validation
     * - Correct header ordering
     * 
     * Note: This configuration class is kept for environment variable parsing only
     */
}