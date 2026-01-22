package com.jitech.mindsync.controller;

import com.jitech.mindsync.model.Users;
import com.jitech.mindsync.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Users user) {
        try {
            return ResponseEntity.ok(authService.registerUser(user));
        } catch (IllegalArgumentException e) {
            // Mengubah error 500 menjadi 400 (Bad Request) dengan pesan yang jelas
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
    Users user = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

    if (user != null) {
        // Buat Map baru agar Java tidak memproses seluruh isi model Users
        return ResponseEntity.ok(Map.of(
            "userId", user.getUserId(),
            "email", user.getEmail(),
            "name", user.getName(),
            "dob", user.getDob(),
            "gender", user.getGender().getGenderName(),
            "occupation", user.getOccupation().getOccupationName()
        ));
    } else {
        return ResponseEntity.status(401).body(Map.of("message", "Invalid credentials"));
    }
}
}