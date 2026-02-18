package com.jitech.mindsync.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        logger.debug("Processing authentication filter for request: {} {}", request.getMethod(), requestUri);

        String token = null;

        // 1. Try to get token from httponly cookie first
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    logger.debug("JWT token found in cookie for request: {}", requestUri);
                    break;
                }
            }
        }

        // 2. If no cookie, fall back to Authorization header (for backwards
        // compatibility)
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // Remove "Bearer " prefix
                logger.debug("JWT token found in Authorization header for request: {}", requestUri);
            }
        }

        // 3. Validate token if found
        if (token != null && jwtProvider.validateToken(token)) {
            String userId = jwtProvider.getUserIdFromToken(token);
            logger.debug("JWT token validated successfully. Authenticating userId: {} for request: {}",
                    userId, requestUri);

            // 4. Authenticate user in Spring Security context
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null,
                    new ArrayList<>());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("User authenticated successfully: userId={} for request: {} {}", userId, request.getMethod(),
                    requestUri);
        } else if (token != null) {
            logger.warn("Invalid JWT token provided for request: {} {}", request.getMethod(), requestUri);
        }

        // 5. Continue filter chain
        filterChain.doFilter(request, response);
    }
}