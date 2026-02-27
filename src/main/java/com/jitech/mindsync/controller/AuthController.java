package com.jitech.mindsync.controller;

import com.jitech.mindsync.dto.RegisterRequest;
import com.jitech.mindsync.model.Users;
import com.jitech.mindsync.service.AuthService;
import com.jitech.mindsync.service.ValkeyService;
import com.jitech.mindsync.dto.LoginRequest;
import com.jitech.mindsync.security.JwtProvider;
import com.jitech.mindsync.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String GUEST_ID_COOKIE_NAME = CookieUtils.GUEST_ID_COOKIE_NAME;

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final ValkeyService valkeyService;

    @Autowired
    public AuthController(AuthService authService, JwtProvider jwtProvider, ValkeyService valkeyService) {
        this.authService = authService;
        this.jwtProvider = jwtProvider;
        this.valkeyService = valkeyService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        logger.info("POST /register - Registration request received for email: {}", request.getEmail());
        try {
            Users user = authService.registerUser(request);

            // Extract guest_id from cookies if present
            String guestId = extractGuestIdFromCookies(httpRequest);

            // Send handover message to Valkey if guest_id exists
            if (guestId != null) {
                String messageId = valkeyService.sendHandoverMessage(guestId, user.getUserId().toString());
                if (messageId != null) {
                    logger.info("Handover message sent for guest_id={} to user_id={}, message_id={}",
                            guestId, user.getUserId(), messageId);
                } else {
                    logger.warn("Failed to send handover message for guest_id={} to user_id={}",
                            guestId, user.getUserId());
                }
            }

            // Generate JWT token for the new user
            String token = jwtProvider.generateToken(user.getUserId().toString());
            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 hours
            jwtCookie.setAttribute("SameSite", "Strict");
            httpResponse.addCookie(jwtCookie);
            logger.debug("JWT cookie set for newly registered userId: {}", user.getUserId());

            // Clear guest_id cookie after handover and JWT set
            clearGuestIdCookie(httpResponse);
            logger.debug("Guest ID cookie cleared for registered user: {}", user.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful");

            Map<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("name", user.getName());

            response.put("data", userData);
            logger.info("POST /register - Registration successful for userId: {}, email: {}",
                    user.getUserId(), user.getEmail());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("POST /register - Registration failed for email: {}. Reason: {}",
                    request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse httpResponse) {
        logger.info("POST /login - Login request received for email: {}", loginRequest.getEmail());
        Users user = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

        if (user != null) {
            String token = jwtProvider.generateToken(user.getUserId().toString());
            logger.debug("JWT token generated for userId: {}", user.getUserId());

            // Set JWT as httponly cookie
            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true); // HTTPS only
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 hours
            jwtCookie.setAttribute("SameSite", "Strict"); // CSRF protection
            httpResponse.addCookie(jwtCookie);
            logger.debug("JWT cookie set for userId: {}", user.getUserId());

            // Clear guest_id cookie upon successful login
            clearGuestIdCookie(httpResponse);
            logger.debug("Guest ID cookie cleared for logged in user: {}", user.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");

            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("email", user.getEmail());
            userData.put("name", user.getName());

            userData.put("dob", user.getDob());
            userData.put("gender", user.getGender() != null ? user.getGender().getGenderName() : null);
            userData.put("occupation", user.getOccupation() != null ? user.getOccupation().getOccupationName() : null);

            response.put("user", userData);

            logger.info("POST /login - Login successful for userId: {}, email: {}",
                    user.getUserId(), user.getEmail());
            return ResponseEntity.ok(response);
        } else {
            logger.warn("POST /login - Login failed for email: {} - Invalid credentials", loginRequest.getEmail());
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Invalid email or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse httpResponse) {
        logger.info("POST /logout - Logout request received");
        // Clear the JWT cookie
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // HTTPS only
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Expire immediately
        jwtCookie.setAttribute("SameSite", "Strict");
        httpResponse.addCookie(jwtCookie);

        // Generate and set a new guest_id cookie immediately upon logout
        String newGuestId = CookieUtils.createAndSetGuestIdCookie(httpResponse);
        logger.info("POST /logout - Logout successful, JWT cookie cleared, new guest_id set: {}", newGuestId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logout successful"));
    }

    /**
     * Helper method to clear the guest_id cookie.
     * Called on successful login and registration.
     */
    private void clearGuestIdCookie(HttpServletResponse httpResponse) {
        Cookie guestIdCookie = new Cookie(GUEST_ID_COOKIE_NAME, null);
        guestIdCookie.setHttpOnly(true);
        guestIdCookie.setSecure(true);
        guestIdCookie.setPath("/");
        guestIdCookie.setMaxAge(0); // Expire immediately
        guestIdCookie.setAttribute("SameSite", "Strict");
        httpResponse.addCookie(guestIdCookie);
    }

    /**
     * Extract guest_id cookie value from request.
     * 
     * @param request The HTTP request
     * @return The guest_id value, or null if not found
     */
    private String extractGuestIdFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (GUEST_ID_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}