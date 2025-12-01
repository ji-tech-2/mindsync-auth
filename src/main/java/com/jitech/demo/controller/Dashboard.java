//package com.jitech.demo.controller;
//
//import com.jitech.demo.model.*;
//import com.jitech.demo.repository.GuestRepository;
//import com.jitech.demo.repository.UserRepository;
//import jakarta.validation.Valid;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@CrossOrigin(origins = "http://localhost:5173")
//public class Dashboard {
//
//    private final UserRepository userRepository;
//
//    public Dashboard(UserRepository userRepository) {this.userRepository = userRepository;}
//
//    @PostMapping("/signIn")
//    public ResponseEntity<ApiResponse> singIn(@Valid @RequestBody LoginRequest temp){
//        // cek username sudah terdaftar ato belum di database
//        Users user = userRepository.findByUsername(temp.getUsername());
//
//        // Username tidak ditemukan di database
//        if(user == null){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "User not found", null));
//        }
//
//        // cek password username yg sudah terdaftar
//        if(user.getPassword().equals(temp.getPassword())){
//            LoginRegisterResponse safeUser = userToSafeUser(user);
//            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "User successfully signed in", safeUser));
//        }
//        // username ada tapi password salah
//        else{
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "Incorrect password", null));
//        }
//    }
//
//    @PostMapping("/register")
//    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest temp){
//        // cek email sudah kepake user lain ato belum
//        if(userRepository.findByEmail(temp.getEmail()) != null){
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, "Email already in used", null));
//        }
//        // cek username sudah kepake user lain ato belum
//        if(userRepository.findByUsername(temp.getUsername()) != null){
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, "Username already in used", null));
//        }
//
//        Users user = new Users();
//        user.set(temp.getEmail());
//        user.setPassword(temp.getPassword());
//        user.setUsername(temp.getUsername());
//        user.setName(temp.getName());
//        user.setDob(temp.getDob());
//        user.setGender(temp.getGender());
//        user.setOccupation(temp.getOccupation());
//        user.setWork_rmt_id(temp.getWorkMode());
//
//        // save ke database
//        userRepository.save(user);
//
//        LoginRegisterResponse safeUser = userToSafeUser(user);
//        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "User registered successfully", safeUser));
//    }
//
//
//    // FUNCTIONS
//    private LoginRegisterResponse userToSafeUser(Users user) {
//        LoginRegisterResponse safeUser = new LoginRegisterResponse();
//        safeUser.setUserId(user.getUserId());
//        safeUser.setUsername(user.getUsername());
//        safeUser.setEmail(user.getEmail());
//        safeUser.setDob(user.getDob());
//        safeUser.setGender(user.getGender());
//        safeUser.setOccupation(user.getOccupation());
//        safeUser.setName(user.getName());
//        safeUser.setWorkMode(user.getWork_rmt_id());
//        return safeUser;
//    }
//
////    private  final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//
////    @PostMapping("/signIn")
////    public ResponseEntity<ApiResponse> singIn(@Valid @RequestBody LoginRequest temp){
////        // cek username sudah terdaftar ato belum di database
////        User user = userRepository.findByUsername(temp.getUsername());
////
////        // Username tidak ditemukan di database
////        if(user == null){
////            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "User not found", null));
////        }
////
////        // cek password username yg sudah terdaftar
////        if(encoder.matches(temp.getPassword(), user.getPassword())){
////            LoginRegisterResponse safeUser = userToSafeUser(user);
////            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "User successfully signed in", safeUser));
////        }
////        // username ada tapi password salah
////        else{
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "Incorrect password", null));
////        }
////    }
////
////    @PostMapping("/register")
////    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest temp){
////        // cek email sudah kepake user lain ato belum
////        if(userRepository.findByEmail(temp.getEmail()) != null){
////            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, "Email already in used", null));
////        }
////        // cek username sudah kepake user lain ato belum
////        if(userRepository.findByUsername(temp.getUsername()) != null){
////            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, "Username already in used", null));
////        }
////
////        User user = new User();
////        user.setEmail(temp.getEmail());
////        user.setPassword(encoder.encode(temp.getPassword()));
////        user.setUsername(temp.getUsername());
////        user.setName(temp.getName());
////        user.setDob(temp.getDob());
////        user.setGender(temp.getGenderId());
////        user.setOccupation(temp.getOccupation());
////
////        // save ke database
////        userRepository.save(user);
////
////        LoginRegisterResponse safeUser = userToSafeUser(user);
////        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "User registered successfully", safeUser));
////    }
//}