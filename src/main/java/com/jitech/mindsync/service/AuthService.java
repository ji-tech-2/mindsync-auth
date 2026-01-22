package com.jitech.mindsync.service;

import com.jitech.mindsync.dto.RegisterRequest;
import com.jitech.mindsync.model.Genders;
import com.jitech.mindsync.model.Occupations;
import com.jitech.mindsync.model.Users;
import com.jitech.mindsync.model.WorkRemotes;
import com.jitech.mindsync.repository.GendersRepository;
import com.jitech.mindsync.repository.UserRepository;
import com.jitech.mindsync.repository.OccupationsRepository;
import com.jitech.mindsync.repository.WorkRemotesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final GendersRepository gendersRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OccupationsRepository occupationsRepository; 
    private final WorkRemotesRepository workRemotesRepository;
    
    @Autowired
    public AuthService(
        UserRepository userRepository,
        GendersRepository gendersRepository,
        OccupationsRepository occupationsRepository,
        WorkRemotesRepository workRemotesRepository,
        BCryptPasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.gendersRepository = gendersRepository;
        this.occupationsRepository = occupationsRepository;
        this.workRemotesRepository = workRemotesRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Users registerUser(RegisterRequest request) {
        // 1. Cek email eksis
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // 2. Ambil Gender (Pastikan Frontend kirim 'Male' atau 'Female')
        Genders gender = gendersRepository.findByGenderName(request.getGender())
            .orElseThrow(() -> new IllegalArgumentException("Invalid gender: " + request.getGender()));

        // 3. Set Default Occupation (Sesuai gambar tabel: 'Student')
        // Karena screening belum dilakukan, kita set default ke Student dulu
        String occName = (request.getOccupation() == null) ? "Student" : request.getOccupation();
        Occupations occupation = occupationsRepository.findByOccupationName(occName)
            .orElseThrow(() -> new IllegalArgumentException("Invalid occupation: " + occName));

        // 4. Set Default Work Remote (Sesuai gambar tabel: 'In-person')
        // Kita pakai 'In-person' sebagai default agar database tidak error null
        String wrName = (request.getWorkRmt() == null) ? "In-person" : request.getWorkRmt();
        WorkRemotes workRmt = workRemotesRepository.findByWorkRmtName(wrName)
        .orElseThrow(() -> new IllegalArgumentException("Invalid work remote: " + wrName));
        
        Users user = new Users();
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setDob(request.getDob());

        // relasi
        user.setGender(gender);
        user.setOccupation(occupation); 
        user.setWorkRmt(workRmt);
        
        return userRepository.save(user);
    }

    public Users login(String email, String password) {
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