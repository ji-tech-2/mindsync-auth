package com.jitech.mindsync.service;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for interacting with Valkey (Redis-compatible) streams.
 * Handles guest-to-user handover messages via streams.
 */
@Service
public class ValkeyService {

    private static final Logger logger = LoggerFactory.getLogger(ValkeyService.class);
    private static final String HANDOVER_STREAM = "handover_stream";
    private static final int MAX_STREAM_LENGTH = 1000;

    @Value("${valkey.url:redis://localhost:6379}")
    private String valkeyUrl;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;

    /**
     * Initialize Valkey connection on startup.
     */
    @PostConstruct
    public void connect() {
        try {
            RedisURI redisUri = RedisURI.create(valkeyUrl);
            redisClient = RedisClient.create(redisUri);
            connection = redisClient.connect();
            logger.info("Successfully connected to Valkey at: {}", maskUrl(valkeyUrl));
        } catch (Exception e) {
            logger.error("Failed to connect to Valkey at: {}. Error: {}. Handover messages will not be sent.",
                    maskUrl(valkeyUrl), e.getMessage());
            // Don't throw exception - allow app to start even if Valkey is unavailable
        }
    }

    /**
     * Close Valkey connection on shutdown.
     */
    @PreDestroy
    public void disconnect() {
        if (connection != null) {
            connection.close();
            logger.info("Closed Valkey connection");
        }
        if (redisClient != null) {
            redisClient.shutdown();
            logger.info("Shut down Valkey client");
        }
    }

    /**
     * Send a handover message to the Valkey stream.
     * 
     * @param guestId The guest_id cookie value
     * @param userId  The newly created user ID
     * @return The stream entry ID, or null if failed
     */
    public String sendHandoverMessage(String guestId, String userId) {
        try {
            RedisCommands<String, String> commands = connection.sync();

            Map<String, String> body = new HashMap<>();
            body.put("guest_id", guestId);
            body.put("user_id", userId);

            // XADD handover_stream MAXLEN ~ 1000 * guest_id <guestId> user_id <userId>
            String messageId = commands.xadd(
                    HANDOVER_STREAM,
                    XAddArgs.Builder.maxlen(MAX_STREAM_LENGTH).approximateTrimming(),
                    body);

            logger.info("Handover message sent to stream '{}': guest_id={}, user_id={}, message_id={}",
                    HANDOVER_STREAM, guestId, userId, messageId);
            return messageId;

        } catch (Exception e) {
            logger.error("Failed to send handover message to Valkey stream. guest_id={}, user_id={}, error={}",
                    guestId, userId, e.getMessage());
            return null;
        }
    }

    /**
     * Check if Valkey connection is active.
     * 
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        try {
            return connection != null && connection.isOpen();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Mask sensitive parts of the URL for logging.
     */
    private String maskUrl(String url) {
        if (url.contains("@")) {
            return url.replaceAll("://([^:]+):([^@]+)@", "://$1:***@");
        }
        return url;
    }
}
