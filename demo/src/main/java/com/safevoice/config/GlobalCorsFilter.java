package com.safevoice.config;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Global CORS Filter - Runs Before Spring Security
 * 
 * This filter applies CORS headers to all requests before they reach Spring Security.
 * Marked with @Component and @Order(Ordered.HIGHEST_PRECEDENCE) to ensure it runs
 * at the very beginning of the servlet filter chain.
 * 
 * Configuration:
 * - Allowed Origins:
 *   * https://projectfrontend17.vercel.app (Production)
 *   * http://localhost:3000 (Local React)
 *   * http://localhost:4200 (Local Angular)
 * - Allowed Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD
 * - Allowed Headers: All standard headers
 * - Credentials: Enabled
 * - Max Age: 3600 seconds (1 hour)
 * 
 * Execution Order:
 * 1. Request arrives
 * 2. GlobalCorsFilter processes (highest precedence)
 * 3. CORS headers added for allowed origins
 * 4. If OPTIONS (preflight), return 204 No Content immediately
 * 5. Otherwise, continue to Spring Security and application
 * 
 * @author Security Team
 * @version 1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalCorsFilter extends OncePerRequestFilter {

    /**
     * List of allowed origins
     */
    private static final String[] ALLOWED_ORIGINS = {
        "https://projectfrontend17.vercel.app",
        "http://localhost:3000",
        "http://localhost:4200"
    };

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
        
        // Check if origin is allowed
        boolean isAllowed = isOriginAllowed(origin);
        
        // Set CORS headers if origin is allowed
        if (isAllowed && origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
            response.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Authorization, X-Requested-With, Accept, X-CSRF-Token");
            response.setHeader("Access-Control-Expose-Headers", "Content-Type, Authorization, X-Total-Count");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
        }

        // Handle preflight OPTIONS request immediately
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        // Continue filter chain for non-OPTIONS requests
        filterChain.doFilter(request, response);
    }

    /**
     * Check if the given origin is in the allowed list
     * 
     * @param origin The origin to check
     * @return true if origin is allowed, false otherwise
     */
    private boolean isOriginAllowed(String origin) {
        if (origin == null) {
            return false;
        }
        for (String allowed : ALLOWED_ORIGINS) {
            if (origin.equals(allowed)) {
                return true;
            }
        }
        return false;
    }
}

