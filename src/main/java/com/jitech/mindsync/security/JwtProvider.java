package com.jitech.mindsync.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

    // Mengambil secret key dari application.properties
    @Value("${mindsync.jwt.secret}")
    private String jwtSecret;

    // Mengambil waktu expired dari application.properties
    @Value("${mindsync.jwt.expiration}")
    private int jwtExpiration;

    // 1. FUNGSI MEMBUAT TOKEN (Digunakan saat user berhasil login)
    public String generateToken(String email) {
        logger.debug("Generating JWT token for email: {}", email);
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        String token = Jwts.builder()
                .setSubject(email) // Menyimpan email di dalam token
                .setIssuedAt(new Date()) // Waktu token dibuat
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Waktu expired
                .signWith(key, SignatureAlgorithm.HS256) // Tanda tangan digital
                .compact();

        logger.info("JWT token generated successfully for email: {}", email);
        return token;
    }

    // 2. FUNGSI VALIDASI (Mengecek apakah token palsu atau sudah kadaluarsa)
    public boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
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
    public String getEmailFromToken(String token) {
        logger.debug("Extracting email from JWT token");
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        String email = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        logger.debug("Email extracted successfully from JWT token: {}", email);
        return email;
    }
}