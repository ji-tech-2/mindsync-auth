package com.jitech.mindsync.security;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    // Base64 encoded private key (env var)
    @Value("${mindsync.jwt.private-key:#{null}}")
    private String privateKeyBase64;

    // Base64 encoded public key (env var)
    @Value("${mindsync.jwt.public-key:#{null}}")
    private String publicKeyBase64;

    // Mengambil waktu expired dari application.properties
    @Value("${mindsync.jwt.expiration}")
    private int jwtExpiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        logger.info("Initializing JWT Provider with RSA keys");
        this.privateKey = loadPrivateKey();
        this.publicKey = loadPublicKey();
        logger.info("JWT Provider initialized successfully with RSA-256 algorithm");
    }

    private PrivateKey loadPrivateKey() throws Exception {
        if (privateKeyBase64 == null || privateKeyBase64.isEmpty()) {
            logger.error("No private key configured");
            throw new IllegalStateException("No private key configured. Set mindsync.jwt.private-key.");
        }

        logger.debug("Loading private key");

        try {
            // Strip PEM headers/footers and whitespace, then decode the Base64 body
            String keyBody = privateKeyBase64
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyBody);

            // Generate private key
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey key = keyFactory.generatePrivate(keySpec);

            logger.info("Private key loaded successfully");
            return key;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Failed to load private key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    private PublicKey loadPublicKey() throws Exception {
        if (publicKeyBase64 == null || publicKeyBase64.isEmpty()) {
            logger.error("No public key configured");
            throw new IllegalStateException("No public key configured. Set mindsync.jwt.public-key.");
        }

        logger.debug("Loading public key");

        try {
            // Strip PEM headers/footers and whitespace, then decode the Base64 body
            String keyBody = publicKeyBase64
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyBody);

            // Generate public key
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey key = keyFactory.generatePublic(keySpec);

            logger.info("Public key loaded successfully");
            return key;

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Failed to load public key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    // 1. FUNGSI MEMBUAT TOKEN (Digunakan saat user berhasil login)
    // Uses PRIVATE KEY for signing
    public String generateToken(String email) {
        logger.debug("Generating JWT token for email: {}", email);

        String token = Jwts.builder()
                .setSubject(email) // Menyimpan email di dalam token
                .setIssuedAt(new Date()) // Waktu token dibuat
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Waktu expired
                .signWith(privateKey, SignatureAlgorithm.RS256) // Tanda tangan digital dengan RSA-256
                .compact();

        logger.info("JWT token generated successfully for email: {}", email);
        return token;
    }

    // 2. FUNGSI VALIDASI (Mengecek apakah token palsu atau sudah kadaluarsa)
    // Uses PUBLIC KEY for verification
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
            logger.debug("JWT token validation successful");
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token validation failed - Token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("JWT token validation failed - Malformed token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn("JWT token validation failed - Unsupported token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warn("JWT token validation failed - Illegal argument: {}", e.getMessage());
        } catch (JwtException e) {
            logger.warn("JWT token validation failed - JWT exception: {}", e.getMessage());
        }
        return false;
    }

    // 3. FUNGSI MENGAMBIL EMAIL (Membaca siapa pemilik token ini)
    // Uses PUBLIC KEY for verification
    public String getEmailFromToken(String token) {
        logger.debug("Extracting email from JWT token");
        String email = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        logger.debug("Email extracted successfully from JWT token: {}", email);
        return email;
    }
}