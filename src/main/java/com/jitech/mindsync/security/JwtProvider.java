package com.jitech.mindsync.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    // Mengambil secret key dari application.properties
    @Value("${mindsync.jwt.secret}")
    private String jwtSecret;

    // Mengambil waktu expired dari application.properties
    @Value("${mindsync.jwt.expiration}")
    private int jwtExpiration;

    // 1. FUNGSI MEMBUAT TOKEN (Digunakan saat user berhasil login)
    public String generateToken(String email) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .setSubject(email) // Menyimpan email di dalam token
                .setIssuedAt(new Date()) // Waktu token dibuat
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Waktu expired
                .signWith(key, SignatureAlgorithm.HS256) // Tanda tangan digital
                .compact();
    }

    // 2. FUNGSI VALIDASI (Mengecek apakah token palsu atau sudah kadaluarsa)
    public boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Jika token salah, expired, atau tidak valid, akan masuk ke sini
            System.out.println("Invalid JWT: " + e.getMessage());
        }
        return false;
    }

    // 3. FUNGSI MENGAMBIL EMAIL (Membaca siapa pemilik token ini)
    public String getEmailFromToken(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}