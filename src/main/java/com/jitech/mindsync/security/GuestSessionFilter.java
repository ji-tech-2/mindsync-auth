package com.jitech.mindsync.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to manage guest session cookies.
 * Creates a guest_id cookie for unauthenticated users who don't already have
 * one.
 */
@Component
public class GuestSessionFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(GuestSessionFilter.class);
    private static final String GUEST_ID_COOKIE_NAME = "guest_id";
    private static final String JWT_COOKIE_NAME = "jwt";
    private static final int GUEST_COOKIE_MAX_AGE = 48 * 60 * 60; // 48 hours in seconds

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        logger.debug("Processing guest session filter for request: {} {}", request.getMethod(), requestUri);

        // Check if cookies exist
        boolean hasJwtCookie = false;
        boolean hasGuestIdCookie = false;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    hasJwtCookie = true;
                } else if (GUEST_ID_COOKIE_NAME.equals(cookie.getName())) {
                    hasGuestIdCookie = true;
                }

                // Early exit if both cookies are found
                if (hasJwtCookie && hasGuestIdCookie) {
                    break;
                }
            }
        }

        // Create guest_id cookie only if neither jwt nor guest_id cookie exists
        if (!hasJwtCookie && !hasGuestIdCookie) {
            String guestId = UUID.randomUUID().toString();

            Cookie guestCookie = new Cookie(GUEST_ID_COOKIE_NAME, guestId);
            guestCookie.setHttpOnly(true);
            guestCookie.setSecure(true); // HTTPS only
            guestCookie.setPath("/");
            guestCookie.setMaxAge(GUEST_COOKIE_MAX_AGE);
            guestCookie.setAttribute("SameSite", "Strict");

            response.addCookie(guestCookie);
            logger.info("Created guest_id cookie with value: {} for request: {} {}",
                    guestId, request.getMethod(), requestUri);
        } else {
            logger.debug("Skipping guest_id creation - hasJwt: {}, hasGuestId: {} for request: {} {}",
                    hasJwtCookie, hasGuestIdCookie, request.getMethod(), requestUri);
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
