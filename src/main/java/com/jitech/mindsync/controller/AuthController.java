package com.jitech.mindsync.controller;

import com.jitech.mindsync.dto.RegisterRequest;
import com.jitech.mindsync.model.Users;
import com.jitech.mindsync.service.AuthService;
import com.jitech.mindsync.dto.LoginRequest;
import com.jitech.mindsync.security.JwtProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = {
    "http://165.22.63.100",
    "http://localhost:3000",
    "http://localhost:5173",
    "http://localhost:8080",
    "http://localhost:8081",
    "http://127.0.0.1:3000",
    "http://127.0.0.1:5173",
    "http://127.0.0.1:8080",
    "http://127.0.0.1:8081"
})
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    
    @Autowired
    public AuthController(AuthService authService, JwtProvider jwtProvider) {
        this.authService = authService;
        this.jwtProvider = jwtProvider;
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            Users user = authService.registerUser(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful");
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", user.getEmail());
            userData.put("name", user.getName());
            
            response.put("data", userData);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Users user = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

        if (user != null) {
            String token = jwtProvider.generateToken(user.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("type", "Bearer");

            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("email", user.getEmail());
            userData.put("name", user.getName());
             
            userData.put("dob", user.getDob()); 
            userData.put("gender", user.getGender() != null ? user.getGender().getGenderName() : null);
            userData.put("occupation", user.getOccupation() != null ? user.getOccupation().getOccupationName() : null);

            response.put("user", userData);

            return ResponseEntity.ok(response);
        }else{
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Invalid email or password"
            ));
        }
    }

    
}