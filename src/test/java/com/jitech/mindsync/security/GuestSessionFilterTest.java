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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GuestSessionFilter Unit Tests")
class GuestSessionFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private GuestSessionFilter guestSessionFilter;

    @BeforeEach
    void setUp() {
        // Default request URI and method for logging
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");
    }

    @Nested
    @DisplayName("Guest ID Cookie Creation Tests")
    class GuestIdCreationTests {

        @Test
        @DisplayName("Should create guest_id cookie when no cookies exist")
        void doFilterInternal_WithNoCookies_ShouldCreateGuestId() throws ServletException, IOException {
            // Given
            when(request.getCookies()).thenReturn(null);

            // When
            guestSessionFilter.doFilterInternal(request, response, filterChain);

            // Then
            ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
            verify(response, times(1)).addCookie(cookieCaptor.capture());
            verify(filterChain, times(1)).doFilter(request, response);

            Cookie capturedCookie = cookieCaptor.getValue();
            assertEquals("guest_id", capturedCookie.getName());
            assertNotNull(capturedCookie.getValue());
            assertTrue(capturedCookie.isHttpOnly());
            assertTrue(capturedCookie.getSecure());
            assertEquals("/", capturedCookie.getPath());
            assertEquals(48 * 60 * 60, capturedCookie.getMaxAge()); // 48 hours
            assertEquals("Strict", capturedCookie.getAttribute("SameSite"));
        }

        @Test
        @DisplayName("Should create guest_id cookie when empty cookies array")
        void doFilterInternal_WithEmptyCookies_ShouldCreateGuestId() throws ServletException, IOException {
            // Given
            when(request.getCookies()).thenReturn(new Cookie[] {});

            // When
            guestSessionFilter.doFilterInternal(request, response, filterChain);

            // Then
            ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
            verify(response, times(1)).addCookie(cookieCaptor.capture());
            verify(filterChain, times(1)).doFilter(request, response);

            Cookie capturedCookie = cookieCaptor.getValue();
            assertEquals("guest_id", capturedCookie.getName());
            assertNotNull(capturedCookie.getValue());
        }

        @Test
        @DisplayName("Should create guest_id cookie when only other cookies exist")
        void doFilterInternal_WithOtherCookies_ShouldCreateGuestId() throws ServletException, IOException {
            // Given
            Cookie[] cookies = {
                    new Cookie("session", "session-value"),
                    new Cookie("preferences", "dark-mode")
            };
            when(request.getCookies()).thenReturn(cookies);

            // When
            guestSessionFilter.doFilterInternal(request, response, filterChain);

            // Then
            ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
            verify(response, times(1)).addCookie(cookieCaptor.capture());

            Cookie capturedCookie = cookieCaptor.getValue();
            assertEquals("guest_id", capturedCookie.getName());
        }

        @Test
        @DisplayName("Should generate valid UUID-4 for guest_id value")
        void doFilterInternal_ShouldGenerateValidUUID() throws ServletException, IOException {
            // Given
            when(request.getCookies()).thenReturn(null);

            // When
            guestSessionFilter.doFilterInternal(request, response, filterChain);

            // Then
            ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(cookieCaptor.capture());

            Cookie capturedCookie = cookieCaptor.getValue();
            String guestId = capturedCookie.getValue();

            // Validate UUID format (8-4-4-4-12)
            assertTrue(guestId.matches(
                    "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"),
                    "Guest ID should be a valid UUID-4 format");
        }
    }

    @Nested
    @DisplayName("Guest ID Cookie Skip Tests")
    class GuestIdSkipTests {

        @Test
        @DisplayName("Should not create guest_id when jwt cookie exists")
        void doFilterInternal_WithJwtCookie_ShouldNotCreateGuestId() throws ServletException, IOException {
            // Given
            Cookie[] cookies = {
                    new Cookie("jwt", "valid.jwt.token")
            };
            when(request.getCookies()).thenReturn(cookies);

            // When
            guestSessionFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response, never()).addCookie(any(Cookie.class));
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not create guest_id when guest_id cookie already exists")
        void doFilterInternal_WithExistingGuestId_ShouldNotCreateAnother() throws ServletException, IOException {
            // Given
            Cookie[] cookies = {
                    new Cookie("guest_id", "existing-guest-id")
            };
            when(request.getCookies()).thenReturn(cookies);

            // When
            guestSessionFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response, never()).addCookie(any(Cookie.class));
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not create guest_id when both jwt and guest_id exist")
        void doFilterInternal_WithBothCookies_ShouldNotCreateGuestId() throws ServletException, IOException {
            // Given
            Cookie[] cookies = {
                    new Cookie("jwt", "valid.jwt.token"),
                    new Cookie("guest_id", "existing-guest-id")
            };
            when(request.getCookies()).thenReturn(cookies);

            // When
            guestSessionFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response, never()).addCookie(any(Cookie.class));
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should not create guest_id when jwt exists among multiple cookies")
        void doFilterInternal_WithJwtAmongMultipleCookies_ShouldNotCreateGuestId()
                throws ServletException, IOException {
            // Given
            Cookie[] cookies = {
                    new Cookie("session", "session-value"),
                    new Cookie("jwt", "valid.jwt.token"),
                    new Cookie("preferences", "dark-mode")
            };
            when(request.getCookies()).thenReturn(cookies);

            // When
            guestSessionFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response, never()).addCookie(any(Cookie.class));
        }

        @Test
        @DisplayName("Should not create guest_id when guest_id exists among multiple cookies")
        void doFilterInternal_WithGuestIdAmongMultipleCookies_ShouldNotCreateAnother()
                throws ServletException, IOException {
            // Given
            Cookie[] cookies = {
                    new Cookie("session", "session-value"),
                    new Cookie("guest_id", "existing-guest-id"),
                    new Cookie("preferences", "dark-mode")
            };
            when(request.getCookies()).thenReturn(cookies);

            // When
            guestSessionFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(response, never()).addCookie(any(Cookie.class));
        }
    }

    @Nested
    @DisplayName("Filter Chain Continuation Tests")
    class FilterChainTests {

        @Test
        @DisplayName("Should always continue filter chain")
        void doFilterInternal_ShouldAlwaysContinueFilterChain() throws ServletException, IOException {
            // Given
            when(request.getCookies()).thenReturn(null);

            // When
            guestSessionFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should continue filter chain even when guest_id exists")
        void doFilterInternal_WithExistingGuestId_ShouldContinueChain() throws ServletException, IOException {
            // Given
            Cookie[] cookies = {
                    new Cookie("guest_id", "existing-guest-id")
            };
            when(request.getCookies()).thenReturn(cookies);

            // When
            guestSessionFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, times(1)).doFilter(request, response);
        }

        @Test
        @DisplayName("Should continue filter chain when jwt exists")
        void doFilterInternal_WithJwt_ShouldContinueChain() throws ServletException, IOException {
            // Given
            Cookie[] cookies = {
                    new Cookie("jwt", "valid.jwt.token")
            };
            when(request.getCookies()).thenReturn(cookies);

            // When
            guestSessionFilter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }
}
