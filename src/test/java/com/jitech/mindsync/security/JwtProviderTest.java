package com.jitech.mindsync.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtProvider Unit Tests")
class JwtProviderTest {

    private JwtProvider jwtProvider;
    private JwtProvider alternateProvider; // For testing with different keys

    private static final int TEST_EXPIRATION = 3600000; // 1 hour in milliseconds
    private static final String TEST_USER_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String TEST_USER_ID_2 = "660e8400-e29b-41d4-a716-446655440001";

    // Test RSA key pair (2048-bit)
    private PrivateKey testPrivateKey;
    private PublicKey testPublicKey;
    private PrivateKey alternatePrivateKey;
    private PublicKey alternatePublicKey;

    @BeforeEach
    void setUp() throws Exception {
        // Generate RSA key pairs for testing
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        testPrivateKey = keyPair.getPrivate();
        testPublicKey = keyPair.getPublic();

        // Generate alternate key pair for testing wrong key scenarios
        KeyPair alternateKeyPair = keyPairGenerator.generateKeyPair();
        alternatePrivateKey = alternateKeyPair.getPrivate();
        alternatePublicKey = alternateKeyPair.getPublic();

        // Initialize primary provider
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "privateKey", testPrivateKey);
        ReflectionTestUtils.setField(jwtProvider, "publicKey", testPublicKey);
        ReflectionTestUtils.setField(jwtProvider, "jwtExpiration", TEST_EXPIRATION);

        // Initialize alternate provider for wrong key tests
        alternateProvider = new JwtProvider();
        ReflectionTestUtils.setField(alternateProvider, "privateKey", alternatePrivateKey);
        ReflectionTestUtils.setField(alternateProvider, "publicKey", alternatePublicKey);
        ReflectionTestUtils.setField(alternateProvider, "jwtExpiration", TEST_EXPIRATION);
    }

    @Nested
    @DisplayName("Token Generation Tests")
    class TokenGenerationTests {

        @Test
        @DisplayName("Should generate valid JWT token")
        void generateToken_ShouldReturnValidToken() {
            // Given
            String userId = TEST_USER_ID;

            // When
            String token = jwtProvider.generateToken(userId);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        }

        @Test
        @DisplayName("Should generate different tokens for different userIds")
        void generateToken_DifferentUserIds_ShouldReturnDifferentTokens() {
            // Given
            String userId1 = TEST_USER_ID;
            String userId2 = TEST_USER_ID_2;

            // When
            String token1 = jwtProvider.generateToken(userId1);
            String token2 = jwtProvider.generateToken(userId2);

            // Then
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate correct token")
        void validateToken_WithValidToken_ShouldReturnTrue() {
            // Given
            String userId = TEST_USER_ID;
            String token = jwtProvider.generateToken(userId);

            // When
            boolean isValid = jwtProvider.validateToken(token);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid token")
        void validateToken_WithInvalidToken_ShouldReturnFalse() {
            // Given
            String invalidToken = "invalid.token.string";

            // When
            boolean isValid = jwtProvider.validateToken(invalidToken);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject null token")
        void validateToken_WithNullToken_ShouldReturnFalse() {
            // When
            boolean isValid = jwtProvider.validateToken(null);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject empty token")
        void validateToken_WithEmptyToken_ShouldReturnFalse() {
            // When
            boolean isValid = jwtProvider.validateToken("");

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject expired token")
        void validateToken_WithExpiredToken_ShouldReturnFalse() throws Exception {
            // Given - Create a provider with very short expiration
            JwtProvider shortExpiryProvider = new JwtProvider();
            ReflectionTestUtils.setField(shortExpiryProvider, "privateKey", testPrivateKey);
            ReflectionTestUtils.setField(shortExpiryProvider, "publicKey", testPublicKey);
            ReflectionTestUtils.setField(shortExpiryProvider, "jwtExpiration", 1); // 1 millisecond

            String token = shortExpiryProvider.generateToken(TEST_USER_ID);

            // Wait for token to expire
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // When
            boolean isValid = shortExpiryProvider.validateToken(token);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should reject token signed with different key")
        void validateToken_WithWrongKey_ShouldReturnFalse() {
            // Given - Create token with different key pair
            String token = alternateProvider.generateToken(TEST_USER_ID);

            // When - Validate with original provider (different public key)
            boolean isValid = jwtProvider.validateToken(token);

            // Then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("UserId Extraction Tests")
    class UserIdExtractionTests {

        @Test
        @DisplayName("Should extract userId from valid token")
        void getUserIdFromToken_WithValidToken_ShouldReturnUserId() {
            // Given
            String userId = TEST_USER_ID;
            String token = jwtProvider.generateToken(userId);

            // When
            String extractedUserId = jwtProvider.getUserIdFromToken(token);

            // Then
            assertThat(extractedUserId).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should extract correct userId for different users")
        void getUserIdFromToken_DifferentUsers_ShouldReturnCorrectUserIds() {
            // Given
            String userId1 = TEST_USER_ID;
            String userId2 = TEST_USER_ID_2;
            String token1 = jwtProvider.generateToken(userId1);
            String token2 = jwtProvider.generateToken(userId2);

            // When
            String extractedUserId1 = jwtProvider.getUserIdFromToken(token1);
            String extractedUserId2 = jwtProvider.getUserIdFromToken(token2);

            // Then
            assertThat(extractedUserId1).isEqualTo(userId1);
            assertThat(extractedUserId2).isEqualTo(userId2);
        }
    }
}
