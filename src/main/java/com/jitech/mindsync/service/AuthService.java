package com.jitech.mindsync.service;

import com.jitech.mindsync.model.Users;
import com.jitech.mindsync.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Logic Register yang sudah ada
    public Users registerUser(Users user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Users login(String email, String password) {
        // Cari user berdasarkan email, bukan username
        Optional<Users> userOpt = userRepository.findByEmail(email); 
        
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user; 
            }
        }
        return null; 
    }
}