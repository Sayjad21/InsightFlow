package com.insightflow.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// Import Jakarta Servlet APIs instead of javax
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List; // Import for List.of()

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        System.out.println("JWT Filter - Processing request: " + requestURI);

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("JWT Filter - Token found in header");
            try {
                username = jwtUtil.extractUsername(token);
                System.out.println("JWT Filter - Username extracted: " + username);
            } catch (Exception e) {
                System.out.println("JWT Filter - Error extracting username: " + e.getMessage());
                // Invalid token - let Spring Security handle it
                chain.doFilter(request, response);
                return;
            }
        } else {
            System.out.println("JWT Filter - No Bearer token found in Authorization header");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("JWT Filter - Validating token for user: " + username);
            try {
                if (jwtUtil.validateToken(token, username)) {
                    System.out.println("JWT Filter - Token is valid, setting authentication");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, List.of()); // Use simple authority list
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    System.out.println("JWT Filter - Token validation failed");
                }
            } catch (Exception e) {
                System.out.println("JWT Filter - Token validation error: " + e.getMessage());
                // Token validation failed - let Spring Security handle it
            }
        }
        chain.doFilter(request, response);
    }
}