package com.jitech.mindsync.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Unit Tests")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Cookie Token Authentication Tests")
    class CookieTokenTests {

        @Test
        @DisplayName("Should authenticate with valid JWT from cookie")
        void doFilterInternal_WithValidCookieToken_ShouldAuthenticate() throws ServletException, IOException {
            // Given
            String token = "valid.jwt.token";
            String email = "test@example.com";
            Cookie jwtCookie = new Cookie("jwt", token);

            when(request.getCookies()).thenReturn(new Cookie[] { jwtCookie });
            when(request.getRequestURI()).thenReturn("/profile");
            when(request.getMethod()).thenReturn("GET");
            when(jwtProvider.validateToken(token)).thenReturn(true);
            when(jwtProvider.getEmailFromToken(token)).thenReturn(email);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertEquals(email, auth.getName());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not authenticate with invalid JWT from cookie")
        void doFilterInternal_WithInvalidCookieToken_ShouldNotAuthenticate() throws ServletException, IOException {
            // Given
            String token = "invalid.jwt.token";
            Cookie jwtCookie = new Cookie("jwt", token);

            when(request.getCookies()).thenReturn(new Cookie[] { jwtCookie });
            when(request.getRequestURI()).thenReturn("/profile");
            when(request.getMethod()).thenReturn("GET");
            when(jwtProvider.validateToken(token)).thenReturn(false);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            verify(filterChain, times(1)).doFilter(request, response);
            verify(jwtProvider, never()).getEmailFromToken(anyString());
        }

        @Test
        @DisplayName("Should handle multiple cookies and find JWT")
        void doFilterInternal_WithMultipleCookies_ShouldFindJwt() throws ServletException, IOException {
            // Given
            String token = "valid.jwt.token";
            String email = "test@example.com";
            Cookie[] cookies = {
                    new Cookie("session", "session-value"),
                    new Cookie("jwt", token),
                    new Cookie("other", "other-value")
            };

            when(request.getCookies()).thenReturn(cookies);
            when(request.getRequestURI()).thenReturn("/profile");
            when(request.getMethod()).thenReturn("GET");
            when(jwtProvider.validateToken(token)).thenReturn(true);
            when(jwtProvider.getEmailFromToken(token)).thenReturn(email);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertEquals(email, auth.getName());
        }
    }

    @Nested
    @DisplayName("Authorization Header Authentication Tests")
    class AuthorizationHeaderTests {

        @Test
        @DisplayName("Should authenticate with valid JWT from Authorization header")
        void doFilterInternal_WithValidHeaderToken_ShouldAuthenticate() throws ServletException, IOException {
            // Given
            String token = "valid.jwt.token";
            String email = "test@example.com";

            when(request.getCookies()).thenReturn(null);
            when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
            when(request.getRequestURI()).thenReturn("/profile");
            when(request.getMethod()).thenReturn("GET");
            when(jwtProvider.validateToken(token)).thenReturn(true);
            when(jwtProvider.getEmailFromToken(token)).thenReturn(email);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertEquals(email, auth.getName());
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not authenticate without Bearer prefix")
        void doFilterInternal_WithoutBearerPrefix_ShouldNotAuthenticate() throws ServletException, IOException {
            // Given
            when(request.getCookies()).thenReturn(null);
            when(request.getHeader("Authorization")).thenReturn("Invalid token");
            when(request.getRequestURI()).thenReturn("/profile");
            when(request.getMethod()).thenReturn("GET");

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            verify(jwtProvider, never()).validateToken(anyString());
        }

        @Test
        @DisplayName("Should prefer cookie over Authorization header")
        void doFilterInternal_WithBothCookieAndHeader_ShouldPreferCookie() throws ServletException, IOException {
            // Given
            String cookieToken = "cookie.jwt.token";
            String email = "test@example.com";
            Cookie jwtCookie = new Cookie("jwt", cookieToken);

            when(request.getCookies()).thenReturn(new Cookie[] { jwtCookie });
            when(request.getRequestURI()).thenReturn("/profile");
            when(request.getMethod()).thenReturn("GET");
            when(jwtProvider.validateToken(cookieToken)).thenReturn(true);
            when(jwtProvider.getEmailFromToken(cookieToken)).thenReturn(email);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(jwtProvider, times(1)).validateToken(cookieToken);
        }
    }

    @Nested
    @DisplayName("No Token Tests")
    class NoTokenTests {

        @Test
        @DisplayName("Should continue filter chain without authentication when no token")
        void doFilterInternal_WithNoToken_ShouldContinueWithoutAuth() throws ServletException, IOException {
            // Given
            when(request.getCookies()).thenReturn(null);
            when(request.getHeader("Authorization")).thenReturn(null);
            when(request.getRequestURI()).thenReturn("/login");
            when(request.getMethod()).thenReturn("POST");

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertNull(auth);
            verify(filterChain, times(1)).doFilter(request, response);
            verify(jwtProvider, never()).validateToken(anyString());
        }

        @Test
        @DisplayName("Should handle null cookies array")
        void doFilterInternal_WithNullCookies_ShouldNotThrow() throws ServletException, IOException {
            // Given
            when(request.getCookies()).thenReturn(null);
            when(request.getHeader("Authorization")).thenReturn(null);
            when(request.getRequestURI()).thenReturn("/public");
            when(request.getMethod()).thenReturn("GET");

            // When/Then - should not throw
            assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should handle empty cookies array")
        void doFilterInternal_WithEmptyCookies_ShouldNotThrow() throws ServletException, IOException {
            // Given
            when(request.getCookies()).thenReturn(new Cookie[] {});
            when(request.getHeader("Authorization")).thenReturn(null);
            when(request.getRequestURI()).thenReturn("/public");
            when(request.getMethod()).thenReturn("GET");

            // When/Then - should not throw
            assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }
}
