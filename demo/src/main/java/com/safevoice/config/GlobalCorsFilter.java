package com.safevoice.config;

import java.io.IOException;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Global CORS Filter Configuration - Runs Before Spring Security
 * 
 * This filter applies CORS headers to all requests before they reach Spring Security.
 * This ensures that preflight OPTIONS requests are properly handled with CORS headers.
 * 
 * Configuration:
 * - Allowed Origins:
 *   * https://projectfrontend17.vercel.app (Production)
 *   * http://localhost:3000 (Local React)
 *   * http://localhost:4200 (Local Angular)
 * - Allowed Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
 * - Allowed Headers: * (all headers)
 * - Credentials: Enabled
 * - Max Age: 3600 seconds (1 hour)
 * 
 * Execution Flow:
 * 1. Request arrives at GlobalCorsFilter FIRST (before Spring Security)
 * 2. CORS headers are added to response
 * 3. If OPTIONS request (preflight), immediately return 200 OK
 * 4. Otherwise, continue filter chain to Spring Security and application
 * 
 * @author Security Team
 * @version 1.0
 */
@Configuration
public class GlobalCorsFilter {

    /**
     * Create and register CORS filter with explicit ordering
     * 
     * FilterRegistrationBean ensures:
     * - Filter runs BEFORE Spring Security (order = -100)
     * - Applied to all URL patterns
     * - Explicitly registered in servlet filter chain
     * 
     * @return FilterRegistrationBean configured CORS filter
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        FilterRegistrationBean<CorsFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new CorsFilter());
        registration.addUrlPatterns("/*");
        registration.setName("GlobalCorsFilter");
        registration.setOrder(-100); // Run BEFORE Spring Security filters
        return registration;
    }

    /**
     * CORS Filter Implementation
     * 
     * Extends OncePerRequestFilter to ensure execution only once per request
     */
    public static class CorsFilter extends OncePerRequestFilter {

        /**
         * Override doFilterInternal to add CORS headers to every request
         * 
         * @param request HttpServletRequest
         * @param response HttpServletResponse
         * @param filterChain FilterChain to continue request processing
         * @throws ServletException if servlet error occurs
         * @throws IOException if IO error occurs
         */
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            String origin = request.getHeader("Origin");
            
            // Always allow from these specific origins
            String[] allowedOrigins = {
                "https://projectfrontend17.vercel.app",
                "http://localhost:3000",
                "http://localhost:4200"
            };
            
            boolean isAllowed = false;
            if (origin != null) {
                for (String allowed : allowedOrigins) {
                    if (origin.equals(allowed)) {
                        isAllowed = true;
                        break;
                    }
                }
            }
            
            // Set CORS headers if origin is allowed
            if (isAllowed) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
                response.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Authorization, X-Requested-With, Accept, X-CSRF-Token");
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setHeader("Access-Control-Expose-Headers", "Content-Type, Authorization, X-Total-Count");
            }
            
            // Handle preflight OPTIONS request
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }

            // Continue filter chain for other requests
            filterChain.doFilter(request, response);
        }
    }
}

