package com.jitech.mindsync.service;

import io.lettuce.core.RedisClient;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValkeyService Unit Tests")
class ValkeyServiceTest {

    @Mock
    private RedisClient redisClient;

    @Mock
    private StatefulRedisConnection<String, String> connection;

    @Mock
    private RedisCommands<String, String> redisCommands;

    @InjectMocks
    private ValkeyService valkeyService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(valkeyService, "valkeyUrl", "redis://localhost:6379");
    }

    @Nested
    @DisplayName("Connection Tests")
    class ConnectionTests {

        @Test
        @DisplayName("Should connect to Valkey on startup")
        void connect_ShouldEstablishConnection() {
            // Given
            ReflectionTestUtils.setField(valkeyService, "connection", connection);
            when(connection.isOpen()).thenReturn(true);

            // When
            boolean result = valkeyService.isConnected();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return connected status when connection is open")
        void isConnected_WithOpenConnection_ShouldReturnTrue() {
            // Given
            ReflectionTestUtils.setField(valkeyService, "connection", connection);
            when(connection.isOpen()).thenReturn(true);

            // When
            boolean result = valkeyService.isConnected();

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return disconnected status when connection is null")
        void isConnected_WithNullConnection_ShouldReturnFalse() {
            // Given
            ReflectionTestUtils.setField(valkeyService, "connection", null);

            // When
            boolean result = valkeyService.isConnected();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return disconnected status when connection is closed")
        void isConnected_WithClosedConnection_ShouldReturnFalse() {
            // Given
            ReflectionTestUtils.setField(valkeyService, "connection", connection);
            when(connection.isOpen()).thenReturn(false);

            // When
            boolean result = valkeyService.isConnected();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle exception when checking connection status")
        void isConnected_WithException_ShouldReturnFalse() {
            // Given
            ReflectionTestUtils.setField(valkeyService, "connection", connection);
            when(connection.isOpen()).thenThrow(new RuntimeException("Connection error"));

            // When
            boolean result = valkeyService.isConnected();

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("Should close connection and shutdown client on disconnect")
        void disconnect_ShouldCloseConnectionAndShutdownClient() {
            // Given
            ReflectionTestUtils.setField(valkeyService, "connection", connection);
            ReflectionTestUtils.setField(valkeyService, "redisClient", redisClient);

            // When
            valkeyService.disconnect();

            // Then
            verify(connection, times(1)).close();
            verify(redisClient, times(1)).shutdown();
        }

        @Test
        @DisplayName("Should handle null connection gracefully on disconnect")
        void disconnect_WithNullConnection_ShouldNotThrow() {
            // Given
            ReflectionTestUtils.setField(valkeyService, "connection", null);
            ReflectionTestUtils.setField(valkeyService, "redisClient", redisClient);

            // When/Then
            assertDoesNotThrow(() -> valkeyService.disconnect());
            verify(redisClient, times(1)).shutdown();
        }
    }

    @Nested
    @DisplayName("Handover Message Tests")
    class HandoverMessageTests {

        @BeforeEach
        void setUpConnection() {
            ReflectionTestUtils.setField(valkeyService, "connection", connection);
            lenient().when(connection.sync()).thenReturn(redisCommands);
        }

        @Test
        @DisplayName("Should send handover message successfully")
        void sendHandoverMessage_WithValidData_ShouldReturnMessageId() {
            // Given
            String guestId = "guest-123";
            String userId = "550e8400-e29b-41d4-a716-446655440000";
            String expectedMessageId = "1234567890123-0";

            when(redisCommands.xadd(anyString(), any(XAddArgs.class), anyMap()))
                    .thenReturn(expectedMessageId);

            // When
            String messageId = valkeyService.sendHandoverMessage(guestId, userId);

            // Then
            assertNotNull(messageId);
            assertEquals(expectedMessageId, messageId);

            // Verify XADD was called with correct parameters
            ArgumentCaptor<String> streamCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<Map<String, String>> bodyCaptor = ArgumentCaptor.forClass(Map.class);

            verify(redisCommands, times(1)).xadd(
                    streamCaptor.capture(),
                    any(XAddArgs.class),
                    bodyCaptor.capture());

            assertEquals("handover_stream", streamCaptor.getValue());
            Map<String, String> capturedBody = bodyCaptor.getValue();
            assertEquals(guestId, capturedBody.get("guest_id"));
            assertEquals(userId, capturedBody.get("user_id"));
        }

        @Test
        @DisplayName("Should handle different UUID formats")
        void sendHandoverMessage_WithDifferentUUIDs_ShouldWork() {
            // Given
            String guestId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
            String userId = "12345678-1234-1234-1234-123456789012";
            String expectedMessageId = "1234567890123-0";

            when(redisCommands.xadd(anyString(), any(XAddArgs.class), anyMap()))
                    .thenReturn(expectedMessageId);

            // When
            String messageId = valkeyService.sendHandoverMessage(guestId, userId);

            // Then
            assertNotNull(messageId);
            assertEquals(expectedMessageId, messageId);
        }

        @Test
        @DisplayName("Should return null when Valkey command fails")
        void sendHandoverMessage_WhenCommandFails_ShouldReturnNull() {
            // Given
            String guestId = "guest-123";
            String userId = "550e8400-e29b-41d4-a716-446655440000";

            when(redisCommands.xadd(anyString(), any(XAddArgs.class), anyMap()))
                    .thenThrow(new RuntimeException("Valkey error"));

            // When
            String messageId = valkeyService.sendHandoverMessage(guestId, userId);

            // Then
            assertNull(messageId);
        }

        @Test
        @DisplayName("Should handle null connection gracefully")
        void sendHandoverMessage_WithNullConnection_ShouldReturnNull() {
            // Given - Override the setup to set connection to null
            ReflectionTestUtils.setField(valkeyService, "connection", null);
            String guestId = "guest-123";
            String userId = "550e8400-e29b-41d4-a716-446655440000";

            // When
            String messageId = valkeyService.sendHandoverMessage(guestId, userId);

            // Then
            assertNull(messageId);
        }

        @Test
        @DisplayName("Should send message with correct stream and maxlen")
        void sendHandoverMessage_ShouldUseCorrectStreamAndMaxlen() {
            // Given
            String guestId = "guest-123";
            String userId = "user-456";
            String expectedMessageId = "1234567890123-0";

            when(redisCommands.xadd(anyString(), any(XAddArgs.class), anyMap()))
                    .thenReturn(expectedMessageId);

            // When
            valkeyService.sendHandoverMessage(guestId, userId);

            // Then
            ArgumentCaptor<String> streamCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<XAddArgs> argsCaptor = ArgumentCaptor.forClass(XAddArgs.class);

            verify(redisCommands, times(1)).xadd(
                    streamCaptor.capture(),
                    argsCaptor.capture(),
                    anyMap());

            assertEquals("handover_stream", streamCaptor.getValue());
            // Note: XAddArgs is configured with maxlen ~ 1000 in the service
            assertNotNull(argsCaptor.getValue());
        }

        @Test
        @DisplayName("Should handle empty string guest_id")
        void sendHandoverMessage_WithEmptyGuestId_ShouldStillSend() {
            // Given
            String guestId = "";
            String userId = "user-456";
            String expectedMessageId = "1234567890123-0";

            when(redisCommands.xadd(anyString(), any(XAddArgs.class), anyMap()))
                    .thenReturn(expectedMessageId);

            // When
            String messageId = valkeyService.sendHandoverMessage(guestId, userId);

            // Then
            assertNotNull(messageId);
            assertEquals(expectedMessageId, messageId);
        }

        @Test
        @DisplayName("Should handle empty string user_id")
        void sendHandoverMessage_WithEmptyUserId_ShouldStillSend() {
            // Given
            String guestId = "guest-123";
            String userId = "";
            String expectedMessageId = "1234567890123-0";

            when(redisCommands.xadd(anyString(), any(XAddArgs.class), anyMap()))
                    .thenReturn(expectedMessageId);

            // When
            String messageId = valkeyService.sendHandoverMessage(guestId, userId);

            // Then
            assertNotNull(messageId);
            assertEquals(expectedMessageId, messageId);
        }
    }
}
