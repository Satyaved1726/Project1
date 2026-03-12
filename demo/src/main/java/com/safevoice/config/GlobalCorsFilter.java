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

            // Add CORS headers to response for all requests
            String origin = request.getHeader("Origin");
            
            // Allow specific origins
            if (origin != null && (
                    origin.equals("https://projectfrontend17.vercel.app") ||
                    origin.equals("http://localhost:3000") ||
                    origin.equals("http://localhost:4200")
            )) {
                response.setHeader("Access-Control-Allow-Origin", origin);
            }
            
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Expose-Headers", "Content-Type, Authorization");

            // Handle preflight OPTIONS request - return immediately with 200 OK
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }

            // Continue filter chain for non-OPTIONS requests
            filterChain.doFilter(request, response);
        }
    }
}

