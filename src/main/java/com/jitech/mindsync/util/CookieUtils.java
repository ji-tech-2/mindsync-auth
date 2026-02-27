package com.jitech.mindsync.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Utility class for common cookie operations.
 */
public final class CookieUtils {

    public static final String GUEST_ID_COOKIE_NAME = "guest_id";
    public static final int GUEST_COOKIE_MAX_AGE = 48 * 60 * 60; // 48 hours in seconds

    private CookieUtils() {
    }

    /**
     * Generates a new guest ID, builds the guest_id cookie with standard security
     * attributes, adds it to the response, and returns the generated ID.
     *
     * @param response The HTTP response to add the cookie to
     * @return The newly generated guest ID
     */
    public static String createAndSetGuestIdCookie(HttpServletResponse response) {
        String guestId = UUID.randomUUID().toString();
        Cookie guestCookie = new Cookie(GUEST_ID_COOKIE_NAME, guestId);
        guestCookie.setHttpOnly(true);
        guestCookie.setSecure(true);
        guestCookie.setPath("/");
        guestCookie.setMaxAge(GUEST_COOKIE_MAX_AGE);
        guestCookie.setAttribute("SameSite", "Strict");
        response.addCookie(guestCookie);
        return guestId;
    }
}
