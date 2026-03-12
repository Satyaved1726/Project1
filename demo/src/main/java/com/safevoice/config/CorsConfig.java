package com.safevoice.config;

import org.springframework.context.annotation.Configuration;

/**
 * CORS Configuration Marker Class
 * 
 * CORS handling is now managed entirely by GlobalCorsFilter with FilterRegistrationBean
 * This ensures proper filter ordering and execution before Spring Security
 * 
 * GlobalCorsFilter provides:
 * - Servlet-level CORS handling (order = -100)
 * - Origin validation
 * - Proper header configuration
 * - OPTIONS preflight handling
 * 
 * @author Security Team
 * @version 1.0
 */
@Configuration
public class CorsConfig {
}