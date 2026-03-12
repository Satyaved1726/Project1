package com.safevoice.config;

import java.io.IOException;

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
 * This ensures that preflight OPTIONS requests are properly handled with CORS headers.
 * 
 * Configuration:
 * - Allowed Origin: https://projectfrontend17.vercel.app
 * - Allowed Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH
 * - Allowed Headers: * (all headers)
 * - Credentials: Enabled
 * 
 * Execution Flow:
 * 1. Request arrives at GlobalCorsFilter first
 * 2. CORS headers are added to response
 * 3. If OPTIONS request (preflight), immediately return 200 OK
 * 4. Otherwise, continue filter chain to Spring Security and application
 * 
 * @author Security Team
 * @version 1.0
 */
@Component
public class GlobalCorsFilter extends OncePerRequestFilter {

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
        response.setHeader("Access-Control-Allow-Origin", "https://projectfrontend17.vercel.app");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Handle preflight OPTIONS request - return immediately with 200 OK
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Continue filter chain for non-OPTIONS requests
        filterChain.doFilter(request, response);
    }
}
