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
            String email = "test@example.com";

            // When
            String token = jwtProvider.generateToken(email);

            // Then
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        }

        @Test
        @DisplayName("Should generate different tokens for different emails")
        void generateToken_DifferentEmails_ShouldReturnDifferentTokens() {
            // Given
            String email1 = "user1@example.com";
            String email2 = "user2@example.com";

            // When
            String token1 = jwtProvider.generateToken(email1);
            String token2 = jwtProvider.generateToken(email2);

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
            String email = "test@example.com";
            String token = jwtProvider.generateToken(email);

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

            String token = shortExpiryProvider.generateToken("test@example.com");

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
            String token = alternateProvider.generateToken("test@example.com");

            // When - Validate with original provider (different public key)
            boolean isValid = jwtProvider.validateToken(token);

            // Then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("Email Extraction Tests")
    class EmailExtractionTests {

        @Test
        @DisplayName("Should extract email from valid token")
        void getEmailFromToken_WithValidToken_ShouldReturnEmail() {
            // Given
            String email = "test@example.com";
            String token = jwtProvider.generateToken(email);

            // When
            String extractedEmail = jwtProvider.getEmailFromToken(token);

            // Then
            assertThat(extractedEmail).isEqualTo(email);
        }

        @Test
        @DisplayName("Should extract correct email for different users")
        void getEmailFromToken_DifferentUsers_ShouldReturnCorrectEmails() {
            // Given
            String email1 = "user1@example.com";
            String email2 = "user2@example.com";
            String token1 = jwtProvider.generateToken(email1);
            String token2 = jwtProvider.generateToken(email2);

            // When
            String extractedEmail1 = jwtProvider.getEmailFromToken(token1);
            String extractedEmail2 = jwtProvider.getEmailFromToken(token2);

            // Then
            assertThat(extractedEmail1).isEqualTo(email1);
            assertThat(extractedEmail2).isEqualTo(email2);
        }
    }
}
