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

        //   1. Cek duplikasi email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        // 2. Cari Gender (Pastikan sesuai: Male, Female, Non-binary/Other)
        Genders gender = gendersRepository.findByGenderName(request.getGender().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid gender"));

        // 3. Cari Occupation (Sesuai tabel: Employed, Student, dsb.)
        String occName = (request.getOccupation() == null) ? "Student" : request.getOccupation().trim();
        Occupations occupation = occupationsRepository.findByOccupationNameIgnoreCase(occName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid occupation: " + occName));

        // 4. Cari Work Remote (PENTING: Gunakan 'In-person', bukan 'On-site')
        String workRmtName = (request.getWorkRmt() == null) ? "In-person" : request.getWorkRmt().trim();
        WorkRemotes workRmt = workRemotesRepository.findByWorkRmtNameIgnoreCase(workRmtName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid work remote status: " + workRmtName));

        // 5. Simpan User
        Users user = new Users();
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setDob(request.getDob());
        user.setGender(gender);
        user.setOccupation(occupation);
        user.setWorkRmt(workRmt);

        return userRepository.save(user);

    // cek db lokal
        // 1. Cek email eksis...
        // if (userRepository.findByEmail(request.getEmail()).isPresent()) {
        //     throw new IllegalArgumentException("Email already registered");
        // }

        // // 2. Cari Gender
        // Genders gender = gendersRepository.findByGenderName(request.getGender())
        //     .orElseThrow(() -> new IllegalArgumentException("Invalid gender"));

        // // 3. Cari Occupation (Tambahkan ini)
        // Occupations occupation = occupationsRepository.findByOccupationName(request.getOccupation())
        //     .orElseThrow(() -> new IllegalArgumentException("Invalid occupation"));

        // // 4. Cari Work Remote (Tambahkan ini)
        // // Cek jika request workRmt null, berikan default "On-site"
        // String workRmtName = (request.getWorkRmt() == null) ? "On-site" : request.getWorkRmt();

        // WorkRemotes workRmt = workRemotesRepository.findByWorkRmtName(workRmtName   )
        //     .orElseThrow(() -> new IllegalArgumentException("Invalid work remote status"));

        // Users user = new Users();
        // user.setEmail(request.getEmail());
        // user.setUsername(request.getEmail());
        // user.setPassword(passwordEncoder.encode(request.getPassword()));
        // user.setName(request.getName());
        // user.setDob(request.getDob());
        // user.setGender(gender);
        // user.setOccupation(occupation); 
        // user.setWorkRmt(workRmt);

        // return userRepository.save(user);
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